from pymongo import MongoClient
from dotenv import load_dotenv
import os

# Load environment variables from .env file
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)
print(dotenv_path)
MONGO_USERNAME = os.getenv('MONGO_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_PASSWORD')

client = MongoClient('mongodb://mongodb:27017/')

db = client.users
collection = db.users

class User:
    """ User model, this user model is used to store the user data in the database
    Example of a user model:
        "_id":"123456789"
        "username": "John95",
        "email": "John95@fake_email.com",
        "password": "Ahashedpassword"
        "dob": 30,
    """
    def __init__(self, username, email, password, dob):
        self.username = username
        self.email = email
        self.password = password
        self.dob = dob

    def json(self):
        return {
            "username": self.username,
            "email": self.email,
            "password": self.password,
            "dob": self.dob
        }