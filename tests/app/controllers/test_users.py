from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

class TestUsersController(object):
    def test_new_user(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.CREATED

    def test_wrong_password_confirmation(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "otropass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_blank_username(self):
        response = client.post('/register',
                               data='{"username": "", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_invalid_email(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "usertest.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_short_password(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "pw", "password_confirmation": "pw"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
