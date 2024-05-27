from functools import wraps
import jwt
from flask import request, abort
from flask import current_app
from src.models.User import User
import os
from bson import ObjectId  # Import ObjectId from pymongo

JWT_SECRET = os.getenv("JWT_SECRET")


def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        if "Authorization" in request.headers:
            token = request.headers["Authorization"].split(" ")[1]
        if not token:
            return {
                "message": "Authentication Token is missing!",
                "data": None,
                "error": "Unauthorized"
            }, 401
        try:
            data=jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
            current_user = data['username']
            user_id = data['user_id']

            user_data = {
                "username": current_user,
                "user_id": user_id
            }

            if user_data.get('admin', False):
                user_data['admin'] = True

            if current_user is None:
                return {
                "message": "Invalid Authentication token!",
                "data": None,
                "error": "Unauthorized"
            }, 401
        except jwt.ExpiredSignatureError:
            return {
                "message": "Token has expired, please login again!",
                "data": None,
                "error": "Unauthorized"
            }, 401
        except jwt.InvalidTokenError:
            return {
                "message": "Invalid token, please try again with a new token!",
                "data": None,
                "error": "Unauthorized"
            }, 401
        except Exception as e:
            return {
                "message": "Something went wrong while decoding token!",
                "data": None,
                "error": str(e)
            }, 500

        return f(user_data, *args, **kwargs)
    return decorated