from flask import Flask
from flask import request
from pymongo import MongoClient
from dotenv import load_dotenv
import os

app = Flask(__name__) # create an instance of the Flask class referencing this file

ENDPOINT = '/api/users'

# Cargar variables de entorno desde el archivo .env
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)
print(dotenv_path)
MONGO_USERNAME = os.getenv('MONGO_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_PASSWORD')

client = MongoClient('mongodb://' + MONGO_USERNAME + ':' + MONGO_PASSWORD + '@mongodb:27017/')
db = client.users
collection = db.users

@app.route(ENDPOINT, methods=['GET','POST']) # decorator that tells Flask what URL should trigger our function
def index():
    if request.method == 'POST':
        return {'msg':'You are using POST'}
    else:
        # JSON, set response status code to 200
        response = app.response_class(
            response='{"msg":"Welcome to the user microservice"}',
            status=203,
            mimetype='application/json'
        )
        return response

@app.route(ENDPOINT + '/create', methods=['POST'])
def create_user():
    print("Creating user")
    data = request.get_json()
    user = {
        'name': data['name'],
        'email': data['email'],
        'password': data['password']
    }
    collection.insert_one(user)
    return {'msg':'User created'}

@app.route(ENDPOINT + '/all', methods=['GET'])
def all_users():
    users = []
    for user in collection.find():
        user.pop('_id')
        users.append(user)
    return {'users':users}


if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True, port=5001) # run the application on the local development server