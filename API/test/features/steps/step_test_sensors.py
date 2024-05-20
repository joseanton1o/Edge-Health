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
        "beats_per_min":110.0
    }]
def create_user(username, email, password, dob, full_name, context):
    user = {
        "username": username,
        "email": email,
        "password": password,
        "dob": dob,
        "full_name": full_name
    }

    response = requests.post("http://localhost/api/users/create", json=user)
    print(response)
    context.response = response

#######################################################
#################### GIVEN SECTION ####################
@given('I have a qa user authenticated')
def create_qa_user(context):
    random_username = "".join([random.choice("abcdefghijklmnopqrstuvwxyz") for i in range(10)])
    create_user(random_username, "qa@qa.com", "qa", "30/12/1990", "QA TESTER", context)

    context.token = requests.post("http://localhost/api/users/login", json={"username": random_username, "password": "qa"})

    assert context.token.status_code == 200

######################################################
#################### WHEN SECTION ####################

@when('I send new valid sensor data')
def send_new_sensor_data(context):
    for data in SENSOR_DATA:
        response = requests.post("http://localhost/api/sensors/provision", json=data, headers={"Authorization": f"Bearer {context.token.json()['token']}"})
    context.response = response

@when('I send a request to retrieve sensor data')
def send_request_retrieve_sensor_data(context):
    response = requests.get("http://localhost/api/sensors", headers={"Authorization": f"Bearer {context.token.json()['token']}"})
    context.response = response


######################################################
#################### THEN SECTION ####################

@then('the response status code should be 201')
def check_response_status_code(context):
    print(context.response)
    assert context.response.status_code == 201

@then('the response status code should be 200')
def check_response_status_code(context):
    print(context.response)
    assert context.response.status_code == 200

@then('the response status code should be 401')
def check_response_status_code(context):
    print(context.response)
    assert context.response.status_code == 401

@then('the response should contain the number of data I sent')
def check_response_status_code(context):
    print(context.response.json())
    assert len(context.response.json()['user_data']) == len(SENSOR_DATA)