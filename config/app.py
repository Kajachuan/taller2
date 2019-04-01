from os import environ

class Config(object):
    DEBUG = False
    TESTING = False

class ProductionConfig(Config):
    try:
        MONGODB_SETTINGS = {
            'db': 'hypechat',
            'host': environ['MONGODB_URI']
        }
    except KeyError:
        pass

class DevelopmentConfig(Config):
    DEBUG = True
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': 'mongodb://localhost:27017/hypechat'
    }

class TestingConfig(Config):
    TESTING = True
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': 'mongomock://localhost'
    }
