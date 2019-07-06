import firebase_admin
from firebase_admin import credentials
from firebase_admin import messaging
from os import environ, path
from .user import User
from flask import current_app

PRODUCTION = 'production'
CREDENTIALS_PATH = path.join(path.dirname(__file__),'./hypechat-647c1-firebase-adminsdk-bo1d5-77d6497801.json')
SENDER = 'sender'
MESSAGE = 'message'
TIMESTAMP = 'timestamp'
ORGANIZATION = 'organization'
CHANNEL = 'channel'
TYPE = 'type'

class FirebaseApi(object):
    @classmethod
    def start_service(cls):
        cred = credentials.Certificate(CREDENTIALS_PATH)
        default_app = firebase_admin.initialize_app(cred)

    def send_message_to_users(self, list_username, message, organization_name, channel_name):
        if environ['FLASK_ENV'] != PRODUCTION:
            return True
        tokens = self.get_users_tokens(list_username)
        message_payload = message.message if message.type == 'text' or message.type == 'snippet' else ''
        for token in tokens:
            new_message = messaging.Message(
                    data = {
        		SENDER : message.sender,
                MESSAGE : message_payload,
                TIMESTAMP : str(message.timestamp),
                TYPE: message.type,
                ORGANIZATION : organization_name,
                CHANNEL : channel_name,
        		},
        	token = token,
            )
            response = messaging.send(new_message)
        return True

    def send_notification_to_users(self, list_username, title, body):
        if environ['FLASK_ENV'] != PRODUCTION:
            return
        tokens = self.get_users_tokens(list_username)
        for token in tokens:
            message = messaging.Message(
            notification=messaging.Notification(
            title=title,
            body=body,
            ),
            token = token,
            )
            messaging.send(message)

    def get_users_tokens(self, list_username):
        tokens = []
        users = [User.objects.get(username = user_name) for user_name in list_username]
        for user in users:
            token = user.firebase_token
            if token:
                tokens.append(token)
        return tokens
