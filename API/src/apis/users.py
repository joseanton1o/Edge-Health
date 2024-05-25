from flask import Flask
from flask import request
from pymongo import MongoClient
from dotenv import load_dotenv
import os, jwt
from src.models.User import User
import logging
from flask_hashing import Hashing
from bson import ObjectId  # Import ObjectId from pymongo
from src.middleware.auth_middleware import token_required
from src.DAOS.UserDAO import UserDAO


app = Flask(__name__) # create an instance of the Flask class referencing this file
hashing = Hashing(app)

# Configure logging
logging.basicConfig(level=logging.INFO)  # Set the logging level to INFO
# Define a logger
logger = logging.getLogger(__name__)

ENDPOINT = '/api/users'

# Load environment variables from .env file
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)
print(dotenv_path)
MONGO_USERNAME = os.getenv('MONGO_INITDB_ROOT_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_INITDB_ROOT_PASSWORD')
HASH_SALT = os.getenv('HASH_SALT')
JWT_SECRET = os.getenv('JWT_SECRET')

users_dao = None

def set_up_db_connection():
    global users_dao
    mongo_string = f'mongodb://{MONGO_USERNAME}:{MONGO_PASSWORD}@mongodb:27017/'
    logger.info(mongo_string)
    client = MongoClient(mongo_string)
    users_dao = UserDAO(client)


set_up_db_connection()

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

@app.route(ENDPOINT + '/create', methods=['POST'])# Register endpoint
def create_user():
    print("Creating user")



    # Check if the user already exists
    if users_dao.get_by_username(request.json['username']):
        return {'msg':'Username already exists'}, 409 # 409 - Conflict
    
    if users_dao.get_by_email(request.json['email']):
        return {'msg':'Email already exists'}, 409 # 409 - Conflict
    
    if not request.json.get('admin'): # If the user has not specified if the user is an admin, set it to False
        request.json['admin'] = False

    request.json['password'] = hashing.hash_value(request.json['password'], salt=HASH_SALT)
    print(request.json)
    u_id = users_dao.create(request.json)
    return {'msg':'User created', 'id':str(u_id)}, 201

@app.route(ENDPOINT + '/<username>', methods=['DELETE'])
def delete_user(username):
    user = users_dao.get_by_username(username)
    if not user:
        return {'msg':'User not found'}, 404
    
    users_dao.delete(username)
    return {'msg':'User deleted'}, 200

@app.route(ENDPOINT + '/all', methods=['GET'])
@token_required(users_dao)
def all_users(requester_username):
    logger.info(f'User {requester_username} requested all users')
    users = []
    for user in users_dao.collection.find({}):
        user.pop('_id')
        user.pop('password')
        users.append(user)
    return {'users':users}


@app.route(ENDPOINT + '/login', methods=['POST'])
def login():
    user = users_dao.get_by_username(request.json['username'])

    if not user:
        return {'msg':'User not found'}, 404
    if not hashing.check_value(user['password'], request.json['password'], salt=HASH_SALT):
        return {'msg':'Invalid password'}, 401
    
    user.pop('password')

    try:
        logger.info(f'User {user} logged in')
        logger.info(str(user['_id']))
        
        if user['admin']:
            user['token'] = jwt.encode({'user_id':str(user['_id']), 'admin':True }, JWT_SECRET , algorithm='HS256')
        else:
            user['token'] = jwt.encode({'user_id':str(user['_id'])}, JWT_SECRET, algorithm='HS256')
        
        user.pop('_id')
        return user, 200
    except Exception as e:
        logger.error(f'Error generating token: {e}')
        return {'msg':'Could not generate token'}, 500

@app.route(ENDPOINT + '/ping', methods=['GET'])
def health():
    return {'msg':'Healthy'}, 200

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True, port=5001) # run the application on the local development server