from flask import Flask
from flask import request
from src.models.Sensors import Sensors
from src.models.User import User
from src.middleware.auth_middleware import token_required
import json
import logging
from bson import ObjectId  # Import ObjectId from pymongo
from src.DAOS.SensorsDAO import SensorsDAO
from pymongo import MongoClient
from dotenv import load_dotenv
import os
from bson import json_util, ObjectId 
from src.DAOS.UserDAO import UserDAO

app = Flask(__name__) # create an instance of the Flask class referencing this file

ENDPOINT = '/api/sensors'

# Configure logging
logging.basicConfig(level=logging.INFO)  # Set the logging level to INFO
# Define a logger
logger = logging.getLogger(__name__)
# Load environment variables from .env file
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)

MONGO_USERNAME = os.getenv('MONGO_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_PASSWORD')

sensors_dao = None
users_dao = None
def set_up_db_connection():
    global sensors_dao
    global users_dao
    client = MongoClient('mongodb://mongodb:27017/')
    sensors_dao = SensorsDAO(client)
    users_dao = UserDAO(client)

set_up_db_connection()

@app.route(ENDPOINT, methods=['POST']) # decorator that tells Flask what URL should trigger our function
def index():
    if request.method == 'POST':
        return {'msg':'You are using POST'}
    else:
        # JSON, set response status code to 200
        response = app.response_class(
            response='{"msg":"Welcome to the heart rate microservice API"}',
            status=203,
            mimetype='application/json'
        )
        return response

@app.route(ENDPOINT + '/provision', methods=['POST'])
@token_required(users_dao)
def provision(requester_username): # Get the username from the token
    logger.info('Provisioning sensor')
    received_data = request.json

    user_id = str(users_dao.get_by_username(requester_username)['_id'])
    logger.info(user_id)

    received_data['user_id'] = str(user_id)

    # Check if the sensor data is correct
    try:
        sensor_data = Sensors(received_data)
    except Exception as e:
        return {
                'error':'Invalid sensor data', 
                'example_body': Sensors.json_example()
                }, 400
    
    try:
        # Provision the sensor
        sensor_data_id = sensors_dao.create(sensor_data.to_json())

        return {'msg':'Sensor provisioned', 'sensor_data_id':str(sensor_data_id)}, 201
    except Exception as e:
        return {'execption':str(e)}, 500

@app.route(ENDPOINT, methods=['GET'])
@token_required(users_dao)
def get_user_data(requester_username): 
    try:
        user_id = users_dao.get_by_username(requester_username)['_id']
        logger.info(user_id)
        user_data = sensors_dao.get_sensors_by_user_id(str(user_id))

        return {'msg':'User data retrieved', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500

@app.route(ENDPOINT + '/delete_all', methods=['DELETE']) # This is meant to be a protected endpoint that only the fakeuser can access TODO: change to use a middleware
@token_required(users_dao)
def delete_all(requester_username):

    if requester_username != 'fakeuser':
        return {'msg':'You are not authorized to delete all data'}, 401

    try:
        user_id = users_dao.get_by_username(requester_username)['_id']
        logger.info(user_id)
        user_data = sensors_dao._delete_all_sensors_by_user_id(str(user_id))

        return {'msg':'User data deleted', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500


if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True) # run the application on the local development server