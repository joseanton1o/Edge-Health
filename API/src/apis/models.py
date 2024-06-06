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
from src.models.Sensors import Sensors

# TensorFlow and scikit-learn imports
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score
import os
statuses_to_int = {'resting': 0, 'sleeping': 1, 'sport': 2, 'walking': 3}
int_to_statuses = {0: 'resting', 1: 'sleeping', 2: 'sport', 3: 'walking'}
app = Flask(__name__) # create an instance of the Flask class referencing this file

ENDPOINT = '/api/models'

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

@app.route(ENDPOINT + '/train', methods=['POST'])
@token_required
def train(user_data):
    logger.info('Training model')
    received_data = request.json
    logger.info(received_data)

    # If the body is not a JSON having the key "data", return an error 
    if 'data' not in received_data:
        return {'msg':'Invalid data'}, 400
    # If the body is a JSON having the key "data" but the value is not a list, return an error
    if not isinstance(received_data['data'], list):
        return {'msg':'Invalid data'}, 400
    
    # For each element in the list, check if it is model of sensors
    for element in received_data['data']:
        correct_data = Sensors.check_sensor_json(element)

        if not correct_data:
            return {'msg':'Invalid data', 'valid_data_example': Sensors.json_example()}, 400      
    # Search for the user_id inside saved_models folder
    # if theres no user copy and create a new .keras file with the user_id
    # Train the model with the data provided
    # Save the model in the saved_models folder
    # Return a success message

    # open the file in read mode
    user_model = str(user_data['user_id']) + ".keras"

    try:
        loaded_model = tf.keras.models.load_model(os.path.join("saved_model", user_model))
    except ValueError:
        try: # If the model is not found, load the default model
            loaded_model = tf.keras.models.load_model(os.path.join("saved_model", "my_model.keras"))
        except ValueError:
            return {'msg':'No model found'}, 400
    except Exception as e:
        return {'msg':'No model found', "exception": str(e)}, 400

    # Load the data
    dataset = pd.DataFrame(received_data['data'])

    # Separate features and target
    X_vector = dataset.drop(columns=['user_status', 'timestamp'])
    y_vector = dataset['user_status']

    # apply status to int to all rows of the y_vector as a numpy array
    y_vector = y_vector.apply(lambda x: statuses_to_int[x])
    logger.info(y_vector)

    num_classes = 4 
    # One-hot encode the target variable as the model is trained for 4 classes and we 
    y_vector_one_hot = tf.keras.utils.to_categorical(y_vector, num_classes=num_classes)

    loaded_model.fit(X_vector, y_vector_one_hot, epochs=20, batch_size=32)

    # Save the model
    loaded_model.save(os.path.join("saved_model", user_model))

    return {'msg':'Model successfully fine tuned'}, 200

@app.route(ENDPOINT + '/predict', methods=['POST'])
@token_required
def predict(user_data):
    logger.info('Predicting model')
    received_data = request.json
    logger.info(received_data)

    # If the body is not a JSON having the key "data", return an error 
    if 'data' not in received_data:
        return {'msg':'Invalid data, data field missing!'}, 400
    # If the body is a JSON having the key "data" but the value is not a list, return an error
    if not isinstance(received_data.get('data',''), list):
        return {'msg':'Invalid data, data field is not a list!'}, 400
    
    # Search for the user_id inside saved_models folder
    # if theres no user copy and create a new .keras file with the user_id
    # Load the model
    # Predict the data
    # Return the prediction

    # open the file in read mode
    
    user_model = str(user_data['user_id']) + ".keras"

    try:
        logger.info('Loading user model')
        logger.info(os.path.join("saved_model", user_model))
        # Route is absolute
        model_path = "/" + os.path.join("saved_model", user_model)
        loaded_model = tf.keras.models.load_model(model_path)
    except Exception:
        try: # If the model is not found, load the default model
            logger.info('Loading default model')
            logger.info(os.path.join("saved_model", "my_model.keras"))
            model_path = "/" + os.path.join("saved_model", "my_model.keras")
            loaded_model = tf.keras.models.load_model(model_path)
        except Exception as e:
            logger.error(str(e))
            # List all the files in the saved_models folder
            for file in os.listdir("saved_model"):
                logger.info(file)
            logger.error('No model found')
            return {'msg':'No model found', "exception": str(e)}, 500

    # Load the data
    dataset = pd.DataFrame(received_data['data'])


    # Separate features and target
    X_vector = dataset.drop(columns=['user_status', 'timestamp'])
    y_vector = dataset['user_status'] # This will be a list of "none" strings as we are predicting

    y_pred = loaded_model.predict(X_vector)
    predicted_values = {}
    for element in y_pred:
        idx = element.argmax()
        string_element = int_to_statuses[idx]
        if string_element in predicted_values:
            predicted_values[string_element] += 1
        else:
            predicted_values[string_element] = 1

    logger.info(f'Prediction: {predicted_values}')
    return {'msg':'Prediction successfully made', 'prediction':predicted_values}, 200

@app.route(ENDPOINT + '/ping', methods=['GET'])
def health():
    return {'msg':'Healthy'}, 200

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True, port=5002) # run the application on the local development server