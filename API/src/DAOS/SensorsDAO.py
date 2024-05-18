import json
from pymongo import MongoClient
from dotenv import load_dotenv
import os
from bson import json_util, ObjectId 

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

class SensorsDAO:
    def __init__(self, dataBase_connection):
        self.client = dataBase_connection
        self.db = self.client.sensors
        self.collection = self.db.sensors
        self.FROM_REQ_TRANSLATOR = {
            "bpm": "beats_per_min"
        }
        

    def get_sensors_by_user_id(self, user_id):
        """
        Get all the sensors data for a specific user
        Parameters:
            :user_id: str, id of the document of the user
        Returns:
            :sensors: list, list of all the sensors data for the user
        """
        sensors = self.collection.find({"user_id":user_id})
        sensors_list = []

        for sensor in sensors:
            sensor["_id"] = str(sensor["_id"])
            sensor.pop("user_id")
            sensors_list.append(sensor)

        return sensors_list
    
    def create(self, sensor_json):
        new_sensor = self.collection.insert_one(sensor_json)
        return new_sensor.inserted_id

    def _delete_all_sensors_by_user_id(self, user_id):
        """
        Delete all the sensors data for a specific user
        Parameters:
            :user_id: str, id of the document of the user
        Returns:
            :sensors: list, list of all the sensors data for the user
        """
        sensors = self.collection.delete_many({"user_id":user_id})

        while sensors.deleted_count > 0: # Keep deleting until there are no more sensors
            sensors = self.collection.delete_many({"user_id":user_id})

        return sensors.deleted_count
    
    