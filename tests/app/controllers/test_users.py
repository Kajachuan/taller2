from http import HTTPStatus
from taller2.app.app import app
from flask import jsonify

client = app.test_client()

client.post('/register', data='{"username": "IronMan", "email": "tony@stark.com",\
                              "password": "mipass", "password_confirmation": "mipass"}')

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

    def test_set_profile_user_valid(self):
        response = client.post('/profile', data = '{"username" : "IronMan" , "first_name" : "Tony",\
                                        "last_name" : "Stark"}')
        assert response.status_code == HTTPStatus.OK

    def test_get_profile(self):
        client.post('/register', data='{"username": "Hulk", "email": "tony@stark.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')

        client.post('/profile', data = '{"username" : "Hulk" , "first_name" : "Tony",\
                                        "last_name" : "Stark"}')
        response = client.get('/profile/Hulk')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['first_name'] == 'Tony'
        assert response.get_json()['last_name'] == 'Stark'
