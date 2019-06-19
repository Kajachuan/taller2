from http import HTTPStatus
from datetime import datetime
from flask import Blueprint, request, abort, session, current_app, make_response, jsonify
from ..models.user import User
from ..decorators.user_no_banned_required import user_no_banned_required
from ..models.facebook_api import FacebookAPI

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
                                             + user.ban_reason), HTTPStatus.FORBIDDEN))

    session['username'] = user.username
    current_app.logger.info('The user ' + username + ' is logged in')
    return jsonify(message='The user ' + username + ' is logged in'), HTTPStatus.OK

@sessions.route('/login/facebook', methods=['POST'])
def login_facebook():
    token = request.headers['Authorization']
    try:
        facebook_api = FacebookAPI(token)
    except Exception as error:
        current_app.logger.info(str(error))
        abort(make_response(jsonify(message=str(error)), HTTPStatus.UNAUTHORIZED))

    id = facebook_api.get_id()
    user = User.objects(facebook_id=id)
    if user.count() == 0:
        profile = facebook_api.get_profile()
        username = profile['first_name'] + profile['last_name']
        user = User(username=username, email=profile['email'],
                    first_name=profile['first_name'], last_name=profile['last_name'],
                    encoded_image=profile['encoded_image'], facebook_id=profile['id'],
                    creation_date=datetime.now())
        user.save()
        session['username'] = user.username
    else:
        session['username'] = user.first().username

    current_app.logger.info('The user ' + session['username'] + ' is logged in')
    return jsonify(message='The user ' + session['username'] + ' is logged in'), HTTPStatus.OK

@sessions.route('/logout', methods=['DELETE'])
@user_no_banned_required
def logout():
    username = session.pop('username')
    current_app.logger.info('The user ' + username + ' was logged out')
    return jsonify(message='The user ' + username + ' was logged out'), HTTPStatus.OK
