from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()
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
