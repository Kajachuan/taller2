from http import HTTPStatus
from datetime import datetime
from flask import Blueprint, request, abort, session, current_app, make_response, jsonify
from ..models.user import User
from ..decorators.no_ban_required import no_ban_required

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

    if user.ban_date and user.ban_date > datetime.now():
        abort(make_response(jsonify(message='You are banned until '
                                             + str(user.ban_date)
                                             + ' because '
                                             + user.ban_reason), HTTPStatus.UNAUTHORIZED))

    session['username'] = user.username
    current_app.logger.info('The user ' + username + ' is logged in')
    return jsonify(message='The user ' + username + ' is logged in'), HTTPStatus.OK

@sessions.route('/logout', methods=['DELETE'])
@no_ban_required
def logout():
    username = session.pop('username')
    current_app.logger.info('The user ' + username + ' was logged out')
    return jsonify(message='The user ' + username + ' was logged out'), HTTPStatus.OK
