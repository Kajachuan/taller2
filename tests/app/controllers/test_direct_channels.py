from http import HTTPStatus
from taller2.app.app import app
import datetime as dt

client = app.test_client()

class TestDirectChannel(object):
    def setup_class(cls):
        client.post('/register', data='{"username": "Harry", "email": "iron@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass",\
                                        "lat": 0, "lon": 0}')
        client.post('/register', data='{"username": "Ron", "email": "thor@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass",\
                                        "lat": 0, "lon": 0}')
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

    def test_get_user_direct_channels(self):
        response = client.get('/organization/Hogwarts/direct_channels?username=Harry')
        assert response.get_json()['direct_channels'] == ['Ron']
        assert response.status_code == HTTPStatus.OK

    def test_get_messages_empty(self):
        response = client.get('/organization/Hogwarts/direct-channels/message?init=1&end=5&user1=Harry&user2=Ron')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['messages'] == []

    def test_add_messages(self):
        response = client.post('/organization/Hogwarts/direct-channels/messages', data = '{"message":"Hola 1", "from":"Harry", "to":"Ron"}')
        assert response.status_code == HTTPStatus.CREATED
        response = client.post('/organization/Hogwarts/direct-channels/messages', data = '{"message":"Hola 2", "from": "Ron", "to":"Harry"}')
        assert response.status_code == HTTPStatus.CREATED

    def test_get_added_messages(self):
        response = client.get('/organization/Hogwarts/direct-channels/message?init=1&end=5&user1=Harry&user2=Ron')
        assert response.status_code == HTTPStatus.OK
        messages = [message[2] for message in response.get_json()['messages']]
        senders = [message[1] for message in response.get_json()['messages']]
        assert messages == ['Hola 1', 'Hola 2']
        assert senders == ['Harry', 'Ron']
