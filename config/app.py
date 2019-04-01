from os import environ

if environ['FLASK_ENV'] == 'production':
    DEBUG = False
    TESTING = False
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': environ['MONGODB_URI']
    }

elif environ['FLASK_ENV'] == 'development':
    DEBUG = True
    TESTING = False
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': 'mongodb://localhost:27017/hypechat'
    }

else:
    DEBUG = False
    TESTING = True
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': 'mongomock://localhost'
    }
