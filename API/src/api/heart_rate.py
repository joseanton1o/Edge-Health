from flask import Flask
from flask import request

app = Flask(__name__) # create an instance of the Flask class referencing this file

ENDPOINT = '/api/heart_rate'

@app.route(ENDPOINT, methods=['GET','POST']) # decorator that tells Flask what URL should trigger our function
def index():
    if request.method == 'POST':
        return {'msg':'You are using POST'}
    else:
        # JSON, set response status code to 200
        response = app.response_class(
            response='{"msg":"Welcome to the heart rate microservice API"}',
            status=203,
            mimetype='application/json'
        )
        return response

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True) # run the application on the local development server