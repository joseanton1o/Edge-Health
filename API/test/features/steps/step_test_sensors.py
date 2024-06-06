from behave import given, when, then
import requests
import json
import random
#####################
# AUXILIARY FUNCTIONS
#####################
SENSOR_DATA = [{
        "accelerometer_x":1,
        "accelerometer_y":1,
        "accelerometer_z":1,
        "gyroscope_x":1,
        "gyroscope_y":1,
        "gyroscope_z":1,
        "light":20,
        "step_counter":1254,
        "timestamp": 456454343214,
        "beats_per_min":110.0,
        "user_status": "none"
    }]

INVALID_SENSOR_DATA = [{
        "accelerometer_x":1,
        "accelerometer_y":1,
        "accelerometer_z":1,
        "gyroscope_x":1,
        "gyroscope_y":1,
        "gyroscope_z":1,
        "step_counter":1254,
        "beats_per_min":110.0,
        "user_status": "none"
    }]

HOST = "http://127.0.0.1"

def create_user(username, email, password, dob, full_name, context):
    user = {
        "username": username,
        "email": email,
        "password": password,
        "dob": dob,
        "full_name": full_name
    }

    response = requests.post(f"{HOST}/api/users/create", json=user)
    print(response)
    context.response = response

#######################################################
#################### GIVEN SECTION ####################
@given('I have a qa user authenticated')
def create_qa_user(context):
    ranadom_email = "".join([random.choice("abcdefghijklmnopqrstuvwxyz") for i in range(10)]) + "@qa.com"
    random_username = "".join([random.choice("abcdefghijklmnopqrstuvwxyz") for i in range(10)])
    create_user(random_username, ranadom_email, "qa", "30/12/1990", "QA TESTER", context)

    context.token = requests.post(f"{HOST}/api/users/login", json={"username": random_username, "password": "qa"})

    assert context.token.status_code == 200

######################################################
#################### WHEN SECTION ####################

@when('I send new valid sensor data')
def send_new_sensor_data(context):
    for data in SENSOR_DATA:
        response = requests.post(f"{HOST}/api/sensors/provision", json=data, headers={"Authorization": f"Bearer {context.token.json()['token']}"})
    context.response = response

@when('I send new invalid sensor data')
def send_new_invalid_sensor_data(context):
    for data in INVALID_SENSOR_DATA:
        response = requests.post(f"{HOST}/api/sensors/provision", json=data, headers={"Authorization": f"Bearer {context.token.json()['token']}"})
    context.response = response

@when('I send a request to retrieve sensor data')
def send_request_retrieve_sensor_data(context):
    response = requests.get(f"{HOST}/api/sensors", headers={"Authorization": f"Bearer {context.token.json()['token']}"})
    context.response = response

@when('I send new valid sensor data with false auth token')
def send_new_sensor_data_invalid_token(context):
    response = requests.post(f"{HOST}/api/sensors/provision", json=SENSOR_DATA[0], headers={"Authorization": f"Bearer invalid_jwt_token"})
    context.response = response

@when('I send a request to provision sensor data without authentication')
def send_new_sensor_data_no_token(context):
    response = requests.post(f"{HOST}/api/sensors/provision", json=SENSOR_DATA[0])
    context.response = response
######################################################
#################### THEN SECTION ####################

@then('the response status code should be 201')
def check_response_status_code_201(context):
    print(context.response)
    assert context.response.status_code == 201

@then('the response status code should be 200')
def check_response_status_code_200(context):
    print(context.response)
    assert context.response.status_code == 200

@then('the response status code should be 401')
def check_response_status_code_401(context):
    print(context.response)
    assert context.response.status_code == 401

@then('the response status code should be 400')
def check_response_status_code_400(context):
    print(context.response)
    assert context.response.status_code == 400

@then('the response should contain the number of data I sent')
def check_number_of_data_sent(context):
    print(context.response.json())
    assert len(context.response.json()['user_data']) == len(SENSOR_DATA)