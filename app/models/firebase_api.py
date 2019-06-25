import firebase_admin
from firebase_admin import credentials
from firebase_admin import messaging
from os import environ
from .user import User

PRODUCTION = 'production'
CREDENTIALS_PATH = '../config/hypechat-647c1-firebase-adminsdk-bo1d5-77d6497801.json'
SENDER = 'sender'
MESSAGE = 'message'
TIMESTAMP = 'timestamp'
ORGANIZATION = 'organization'
CHANNEL = 'channel'

class FirebaseApi(object):
    def __init__(self):
        if environ['FLASK_ENV'] == PRODUCTION:
            cred = credentials.Certificate(CREDENTIALS_PATH)
            self.default_app  = firebase_admin.initialize_app(cred)

    def send_message_to_users(self, list_username, message, organization_name, channel_name):
        if environ['FLASK_ENV'] != PRODUCTION:
            return True
        tokens = get_users_tokens(list_username)
        for token in tokens:
            message = messaging.Message(
                    data = {
        		SENDER : message.sender,
                MESSAGE : message.message,
                TIMESTAMP : message.timestamp,
                ORGANIZATION : organization_name,
                CHANNEL : channel_name,
        		},
        	token = token,
            )
            response = messaging.send(message)
        return True

    def send_notification_to_user(self, username, organization_name, channel_name):
        user = User.objects.get(username = username)
        token = user.firebase_token
        if environ['FLASK_ENV'] == PRODUCTION:
            message = messaging.Message(
            notification=messaging.Notification(
            title='Te mencionaron en %s'%channel_name,
            body='Fuiste mencionado en %s en el canal %s'%(organization_name, channel_name),
            ),
            token = token,
            )
            messaging.send(message)

    def get_users_tokens(list_username):
        tokens = []
        users = [User.objects.get(username) for username in list_username]
        for user in users:
            token = user.firebase_token
            if token:
                tokens.append(token)
        return tokens
