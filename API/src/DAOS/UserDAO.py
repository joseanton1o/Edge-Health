from pymongo import MongoClient
from dotenv import load_dotenv
import os

class UserDAO:
    """ User model, this user model is used to store the user data in the database
    Example of a user model:
        "_id":"123456789"
        "username": "John95",
        "email": "John95@fake_email.com",
        "password": "Ahashedpassword"
        "dob": 30,
    """
    def __init__(self, dataBase_connection):
        self.client = dataBase_connection
        self.db = self.client.users
        self.collection = self.db.users
    
    def get_by_id(self, _id):
        user = self.collection.find_one({"_id":_id})
        return user
    
    def get_by_username(self, username):
        user = self.collection.find_one({"username":username})
        return user
    
    def create(self, user_json):
        new_user = self.collection.insert_one(user_json)
        return new_user.inserted_id

    def update(self, username, updated_content):
        user = self.collection.find_one({"username":username})
        if user is None:
            return None
        self.collection.update_one({"username":username}, {"$set":updated_content})
        return self.collection.find_one({"username":username})

    def delete(self, username):
        user = self.collection.find_one({"username":username})
        if user is None:
            return None
        self.collection.delete_one({"username":username})
        return self.collection.find_one({"username":username})
