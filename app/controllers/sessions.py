from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app, make_response, jsonify
from ..models.user import User

sessions = Blueprint('sessions', __name__)

@sessions.route('/login', methods=['POST'])
def login():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    password = data['password']

    user = User.authenticate(username, password)
    if not user:
        current_app.logger.info('The username or password are wrong')
        abort(make_response(jsonify(message='The username or password are wrong'), HTTPStatus.BAD_REQUEST))

    session['username'] = user.username
    current_app.logger.info('The user ' + username + ' is logged in')
    return jsonify(message='The user ' + username + ' is logged in'), HTTPStatus.OK

@sessions.route('/logout', methods=['DELETE'])
def logout():
    username = session.pop('username')
    current_app.logger.info('The user ' + username + ' was logged out')
    return jsonify(message='The user ' + username + ' was logged out'), HTTPStatus.OK
