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

MONGO_USERNAME = os.getenv('MONGO_INITDB_ROOT_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_INITDB_ROOT_PASSWORD')

sensors_dao = None
def set_up_db_connection():
    global sensors_dao
    mongo_string = f'mongodb://{MONGO_USERNAME}:{MONGO_PASSWORD}@mongodb:27017/'
    print(mongo_string)
    client = MongoClient(mongo_string)
    sensors_dao = SensorsDAO(client)
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
@token_required
def provision(user_data): # Get the username from the token
    logger.info('Provisioning sensor')
    received_data = request.json

    user_id = user_data.get('user_id', None)
    logger.info(user_id)
    if user_id is None:
        return {
            'error':'Token malformed, user id in jwt is missing, try to relogin'
            }, 500
    
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
@token_required
def get_user_data(user_data): 
    try:
        user_id = user_data.get('user_id', None)
        logger.info(user_id)
        if user_id is None:
            return {'error':'Token malformed, user id in jwt is missing, try to relogin'}, 500
        user_data = sensors_dao.get_sensors_by_user_id(str(user_id))

        return {'msg':'User data retrieved', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500

@app.route(ENDPOINT + '/delete_all', methods=['DELETE']) 
@token_required
def delete_all(user_data):
    
    if user_data.get('Admin', False):
        return {'msg':'You are not authorized to delete all data'}, 401

    try:
        user_id = user_data.get('user_id', None)

        if user_id is None:
            return {'error':'Token malformed'}, 500

        logger.info(user_id)
        user_data = sensors_dao._delete_all_sensors_by_user_id(str(user_id))

        return {'msg':'User data deleted', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500

@app.route(ENDPOINT + '/ping', methods=['GET'])
def health():
    return {'msg':'Healthy'}, 200

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True) # run the application on the local development server