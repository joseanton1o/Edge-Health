"""
Feature: Users API

    Background:
        Given I have qa user inserted in the database

    Scenario: Login with invalid credentials
        When I login with invalid credentials
        Then I should get a 401 status code

    Scenario: Login with valid credentials
        When I login with valid credentials
        Then I should get a 200 status code

    Scenario: Accessing a protected route without a token
        When I access a protected route without a token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with an invalid token
        When I access a protected route with an invalid token
        Then I should get a 401 status code

    Scenario: Accessing a protected route with a valid token
        When I access a protected route with a valid token
        Then I should get a 200 status code

    
"""

from behave import given, when, then
import requests
import json
import random
#####################
# AUXILIARY FUNCTIONS
#####################

def create_user(username, email, password, dob, full_name, context):
    user = {
        "username": username,
        "email": email,
        "password": password,
        "dob": dob,
        "full_name": full_name
    }

    response = requests.post("http://localhost/api/users/create", json=user)
    context.response = response


#######################################################
#################### GIVEN SECTION ####################

@given('I have qa user inserted in the database')
def create_qa_user(context):
    random_username = "".join([random.choice("abcdefghijklmnopqrstuvwxyz") for i in range(10)])
    create_user(random_username, "qa@email.com", "qa", "30/12/1990", "QA TESTER", context)
    context.username = random_username
    assert context.response.status_code == 201


######################################################
#################### WHEN SECTION ####################
@when('I login with invalid credentials')
def login_with_bad_credentials(context):
    user = {
        "username": context.username,
        "password": "badpassword"
    }
    response = requests.post("http://localhost/api/users/login", json=user)
    context.response = response

@when('I login with valid credentials')
def login_with_valid_credentials(context):
    user = {
        "username": context.username,
        "password": "qa"
    }
    response = requests.post("http://localhost/api/users/login", json=user)
    context.response = response

@when('I access a protected route without a token')
def access_no_token(context):
    response = requests.get("http://localhost/api/users/all")
    context.response = response


@when('I access a protected route with an invalid token')
def access_invalid_token(context):
    response = requests.get("http://localhost/api/users/all", headers={"Authorization": "Bearer invalidtoken"})
    context.response = response



@when('I access a protected route with a valid token')
def access_valid_token(context):
    user = {
        "username": context.username,
        "password": "qa"
    }
    response = requests.post("http://localhost/api/users/login", json=user)
    token = response.json()["token"]
    response = requests.get("http://localhost/api/users/all", headers={"Authorization": f"Bearer {token}"})
    context.response = response

######################################################
#################### THEN SECTION ####################
@then('I should get a 200 status code')
def step_impl(context):

    assert context.response.status_code == 200

@then('I should get a 401 status code')
def step_impl(context):
    assert context.response.status_code == 401