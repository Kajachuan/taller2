from os import path
from http import HTTPStatus
from base64 import b64encode
from taller2.app.app import app

client = app.test_client()

file_path = path.join(path.dirname(__file__), '../../../app/static/img/default_image.png')
file = open(path.abspath(file_path), 'rb')
img = b64encode(file.read()).decode()

client.post('/register', data='{"username": "testlogin", "email": "user@test.com",\
                                "password": "mipass", "password_confirmation": "mipass"}')

class TestSessionsController(object):
    def test_correct_login(self):
        response = client.post('/login', data='{"username": "testlogin", "password": "mipass"}')

        assert response.status_code == HTTPStatus.OK
        assert response.get_json() == {'message': 'The user testlogin is logged in'}

    def test_wrong_user(self):
        response = client.post('/login', data='{"username": "cualquiera", "password": "mipass"}')

        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username or password are wrong'}

    def test_wrong_password(self):
        response = client.post('/login', data='{"username": "testlogin", "password": "malpass"}')

        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username or password are wrong'}

    def test_correct_logout(self):
        client.post('/login', data='{"username": "testlogin", "password": "mipass"}')
        response = client.delete('/logout')

        assert response.status_code == HTTPStatus.OK
        assert response.get_json() == {'message': 'The user testlogin was logged out'}

    def test_wrong_facebook_login_token(self):
        response = client.post('/login/facebook', headers={'Authorization': 'wrong_token'})
        assert response.status_code == HTTPStatus.UNAUTHORIZED
        assert response.get_json() == {'message': 'Invalid OAuth access token'}

    def test_correct_facebook_login(self):
        response = client.post('/login/facebook', headers={'Authorization': 'correct_token'})
        assert response.status_code == HTTPStatus.OK
        assert response.get_json() == {'message': 'The user MyNameMyLastName is logged in'}

    def test_get_profile_after_facebook_login(self):
        client.post('/login/facebook', headers={'Authorization': 'correct_token'})
        response = client.get('/profile/MyNameMyLastName')
        assert response.status_code == HTTPStatus.OK
        response = response.get_json()
        assert response['first_name'] == 'MyName'
        assert response['last_name'] == 'MyLastName'
        assert response['email'] == 'test@user.com'
        assert response['image'] == img
