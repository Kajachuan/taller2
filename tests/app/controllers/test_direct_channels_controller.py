from http import HTTPStatus
from taller2.app.app import app
import datetime as dt

client = app.test_client()

class TestDirectChannel(object):
    def setup_class(cls):
        client.post('/register', data='{"username": "Harry", "email": "iron@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/register', data='{"username": "Ron", "email": "thor@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/login', data='{"username": "Harry", "password": "mipass"}')
        client.post('/organization', data = '{"name" : "Hogwarts"}')
        client.post('/organization/Hogwarts/invite', data = '{"username" : "Ron" }')
        client.post('/login', data = '{"username" : "Ron", "password" : "mipass"}')
        invitations = client.get('/profile/Ron/invitations')
        token = list(invitations.get_json()['invitations'].keys())[0]
        organization = invitations.get_json()['invitations'][token]
        response = client.post('/organization/'+organization+'/accept-invitation', data = '{"token" : "'+token+'"}')
        response = client.get('/organization/Hogwarts/members')
        assert 'Ron' in response.get_json()['members']
        client.post('/login', data='{"username": "Harry", "password": "mipass"}')

    def test_create_direct_channel(self):
        response = client.post('/organization/Hogwarts/direct-channels', data = '{"members":"Harry,Ron"}')
        assert response.get_json()['message'] == 'Direct channel created'
        assert response.status_code == HTTPStatus.CREATED
