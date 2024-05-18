from flask import Flask
from flask import request
from src.models.Sensors import Sensors
from src.models.User import User
from src.middleware.auth_middleware import token_required
import json
import logging
from bson import ObjectId  # Import ObjectId from pymongo

app = Flask(__name__) # create an instance of the Flask class referencing this file

ENDPOINT = '/api/sensors'
# Configure logging
logging.basicConfig(level=logging.INFO)  # Set the logging level to INFO
# Define a logger
logger = logging.getLogger(__name__)

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
def provision(requester_username): # Get the username from the token
    logger.info('Provisioning sensor')
    received_data = request.json
    print(received_data)
    logger.info(received_data)
    logger.info(requester_username)
    user_id = str(User.get_by_username(requester_username)['_id'])
    logger.info(user_id)

    received_data['user_id'] = str(user_id)

    logger.info(received_data)
    # Check if the sensor data is correct
    if not Sensors.check_sensor_json(received_data):
        return {
                'error':'Invalid sensor data', 
                'example_body':Sensors.json_example()
                }, 400
    
    try:
        # Provision the sensor
        sensor_data_id = Sensors.create(received_data)

        return {'msg':'Sensor provisioned', 'sensor_data_id':str(sensor_data_id)}, 201
    except Exception as e:
        return {'execption':str(e)}, 500

@app.route(ENDPOINT, methods=['GET'])
@token_required
def get_user_data(requester_username): 
    try:
        user_id = User.get_by_username(requester_username)['_id']
        logger.info(user_id)
        user_data = Sensors.get_sensors_by_user_id(str(user_id))

        return {'msg':'User data retrieved', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500

@app.route(ENDPOINT + '/delete_all', methods=['DELETE']) # This is meant to be a protected endpoint that only the fakeuser can access TODO: change to use a middleware
@token_required
def delete_all(requester_username):

    if requester_username != 'fakeuser':
        return {'msg':'You are not authorized to delete all data'}, 401

    try:
        user_id = User.get_by_username(requester_username)['_id']
        logger.info(user_id)
        user_data = Sensors._delete_all_sensors_by_user_id(str(user_id))

        return {'msg':'User data deleted', 'user_data':user_data}, 200
    except Exception as e:
        return {'msg':str(e)}, 500



if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True) # run the application on the local development server