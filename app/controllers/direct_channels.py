import requests
from os import environ
from http import HTTPStatus
from datetime import datetime
from flask import Blueprint, request, abort, session, current_app, jsonify
from ..models.organization import Organization
from ..models.user import User
from ..models.message import Message
from ..models.direct_channel import DirectChannel
from ..decorators.user_no_banned_required import user_no_banned_required
from ..decorators.organization_no_banned_required import organization_no_banned_required
from ..models.firebase_api import FirebaseApi

direct_channels = Blueprint('direct_channels', __name__)

@direct_channels.app_errorhandler(Organization.DoesNotExist)
@direct_channels.app_errorhandler(User.DoesNotExist)
def inexistent_handler(e):
    error_messages = {'Organization matching query does not exist.' : 'Organization does not exist',
                      'User matching query does not exist.' : 'User does not exist'}
    current_app.logger.debug('Error: %s',error_messages[e.args[0]])
    return jsonify(message = error_messages[e.args[0]]), HTTPStatus.BAD_REQUEST

@direct_channels.route('/organization/<organization_name>/direct-channels', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def create_direct_channel(organization_name):
    data = request.get_json(force=True)
    organization = Organization.objects.get(organization_name = organization_name)
    members_username = data['members'].split(',')
    members = []
    for username in members_username:
        members.append(User.objects.get(username = username))
    new_direct_channel = DirectChannel(members = members)
    new_direct_channel.save()
    organization.update(push__direct_channels = new_direct_channel)
    return jsonify(message = 'Direct channel created'), HTTPStatus.CREATED

@direct_channels.route('/organization/<organization_name>/direct_channels', methods=['GET'])
@user_no_banned_required
@organization_no_banned_required
def get_user_direct_channels(organization_name):
    username = request.args['username']
    direct_channels = []
    organization = Organization.objects.get(organization_name = organization_name)
    for dchannel in organization.direct_channels:
        if username in dchannel.members_username():
            direct_channels.append(dchannel.display_name(username))
    return jsonify(direct_channels = direct_channels)
