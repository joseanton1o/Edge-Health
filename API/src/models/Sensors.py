import json
from pymongo import MongoClient
from dotenv import load_dotenv
import os
from bson import json_util, ObjectId 

# Load environment variables from .env file
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)
print(dotenv_path)
MONGO_USERNAME = os.getenv('MONGO_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_PASSWORD')

FROM_REQ_TRANSLATOR = {
    "bpm": "beats_per_min"
}

client = MongoClient('mongodb://mongodb:27017/')

db = client.sensors
collection = db.sensors
"""
{
    "_id":"123456789",
    "user_id": "123456789",
    "accelerometer_x":1,
    "accelerometer_y":1,
    "accelerometer_z":1,
    "gyroscope_x":1,
    "gyroscope_y":1,
    "gyroscope_z":1,
    "light":20,
    "step_counter":1254,
    "beats_per_min":110.0
}
"""

# TODO: Change this class to be a singleton DAO class, we won't store anything in the class

class Sensors:
    def __init__(self, data):
        if not self.check_sensor_json(data):
            raise Exception("Invalid sensor data")

        self.user_id = data["user_id"]
        self.accelerometer_x = data["accelerometer_x"]
        self.accelerometer_y = data["accelerometer_y"]
        self.accelerometer_z = data["accelerometer_z"]
        self.gyroscope_x = data["gyroscope_x"]
        self.gyroscope_y = data["gyroscope_y"]
        self.gyroscope_z = data["gyroscope_z"]
        self.light = data["light"]
        self.step_counter = data["step_counter"]
        self.beats_per_min = data["beats_per_min"]


    @classmethod
    def get_sensors_by_user_id(cls, user_id):
        """
        Get all the sensors data for a specific user
        Parameters:
            :user_id: str, id of the document of the user
        Returns:
            :sensors: list, list of all the sensors data for the user
        """
        sensors = collection.find({"user_id":user_id})
        sensors_list = []

        for sensor in sensors:
            sensor["_id"] = str(sensor["_id"])
            sensor.pop("user_id")
            sensors_list.append(sensor)

        return sensors_list
    
    @classmethod
    def create(cls, sensor_json):
        new_sensor = collection.insert_one(sensor_json)
        return new_sensor.inserted_id
    
    @classmethod
    def check_sensor_json(cls, sensor_json):
        if "user_id" not in sensor_json:
            return False
        if "accelerometer_x" not in sensor_json:
            return False
        if "accelerometer_y" not in sensor_json:
            return False
        if "accelerometer_z" not in sensor_json:
            return False
        if "gyroscope_x" not in sensor_json:
            return False
        if "gyroscope_y" not in sensor_json:
            return False
        if "gyroscope_z" not in sensor_json:
            return False
        if "light" not in sensor_json:
            return False
        if "step_counter" not in sensor_json:
            return False
        if "beats_per_min" not in sensor_json:
            return False
        if "timestamp" not in sensor_json:
            return False
        # Check if the number of keys is correct
        if len(sensor_json.keys()) != 11:
            return False
        
        return True
    
    @classmethod
    def json_example(cls):
        return {
            "user_id": "123456789",
            "accelerometer_x":1,
            "accelerometer_y":1,
            "accelerometer_z":1,
            "gyroscope_x":1,
            "gyroscope_y":1,
            "gyroscope_z":1,
            "light":20,
            "step_counter":1254,
            "beats_per_min":110.0,
            "timestamp": "2021-06-01T13:00:00Z"
        }
    
    @classmethod
    def _delete_all_sensors_by_user_id(cls, user_id):
        """
        Delete all the sensors data for a specific user
        Parameters:
            :user_id: str, id of the document of the user
        Returns:
            :sensors: list, list of all the sensors data for the user
        """


        sensors = collection.delete_many({"user_id":user_id})

        while sensors.deleted_count > 0: # Keep deleting until there are no more sensors
            sensors = collection.delete_many({"user_id":user_id})

        return sensors.deleted_count
    
    