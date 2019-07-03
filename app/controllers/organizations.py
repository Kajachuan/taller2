import requests
from os import environ
from http import HTTPStatus
from datetime import datetime
from flask import Blueprint, request, abort, session, current_app, jsonify
from ..models.organization import Organization
from ..models.user import User
from ..models.message import Message
from ..decorators.user_no_banned_required import user_no_banned_required
from ..decorators.organization_no_banned_required import organization_no_banned_required
from ..models.firebase_api import FirebaseApi

organizations = Blueprint('organizations', __name__)

@organizations.app_errorhandler(Organization.DoesNotExist)
@organizations.app_errorhandler(User.DoesNotExist)
def inexistent_organization_handler(e):
    error_messages = {'Organization matching query does not exist.' : 'Organization does not exist',
                      'User matching query does not exist.' : 'User does not exist'}
    current_app.logger.debug('Error: %s',error_messages[e.args[0]])
    return jsonify(message = error_messages[e.args[0]]), HTTPStatus.BAD_REQUEST

@organizations.route('/organization', methods=['POST'])
@user_no_banned_required
def create():
    data = request.get_json(force=True)
    organization_name = data['name']
    current_app.logger.debug('The organization name is: ' + organization_name)

    owner = User.objects.get(username = session['username'])
    new_organization = Organization(organization_name = organization_name, owner = owner, creation_date = datetime.now())

    try:
        new_organization.save()
        current_app.logger.debug('Organization created')
    except:
        current_app.logger.debug('Failed to create Organization')
        return jsonify(message = 'Failed to create'),HTTPStatus.BAD_REQUEST
    new_organization.update(push__members = owner)
    User.add_to_organization(owner.username, organization_name)
    return jsonify(message = 'Organization created'),HTTPStatus.CREATED

@organizations.route('/organization/<organization_name>', methods = ['GET'])
def get_info(organization_name):
    try:
        organization = Organization.objects.get(organization_name = organization_name)
    except:
        current_app.logger.info('The organization does not exist')
        abort(HTTPStatus.BAD_REQUEST)

    name = organization.organization_name
    owner = organization.owner.username
    ubication = organization.ubication
    image = organization.encoded_image
    description = organization.description
    welcome_message = organization.welcome_message
    ban_date = organization.ban_date
    ban_reason = organization.ban_reason
    return jsonify(name=name, owner=owner, ubication=ubication, image=image,
                   description=description, welcome_message=welcome_message,
                   ban_date=ban_date, ban_reason=ban_reason), HTTPStatus.OK

@organizations.route('/organization/<organization_name>', methods = ['POST'])
@user_no_banned_required
@organization_no_banned_required
def change_info(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = session['username']
    if not organization.is_owner(User.objects.get(username = username)):
        return jsonify(message = 'You are not the owner'), HTTPStatus.FORBIDDEN
    data = request.get_json(force = True)
    ubication = data.get('ubication', organization.organization_name)
    image = data.get('image', organization.encoded_image)
    description = data.get('description', organization.description)
    welcome_message = data.get('welcome_message', organization.welcome_message)
    organization.update(ubication = ubication)
    organization.update(encoded_image = image)
    organization.update(description = description)
    organization.update(welcome_message = welcome_message)
    return jsonify(message = 'Information changed'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/invite', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def send_invitation(organization_name):
    data = request.get_json(force = True)
    username = data['username']
    organization = Organization.objects.get(organization_name = organization_name)
    user = User.objects.get(username = username)
    if organization.is_member(user):
        return jsonify(message = 'User is already a member'),HTTPStatus.BAD_REQUEST
    token = organization.invite_user(user)
    User.invite(username, token, organization_name)
    organization.update(**{'set__pending_invitations__' + token: user.username})
    current_app.logger.debug('User %s invited to organization %s',user.username, organization_name)
    return jsonify(message = 'Sent invitation'),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/accept-invitation', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def accept(organization_name):
    data = request.get_json(force = True)
    token = data['token']
    organization = Organization.objects.get(organization_name = organization_name)
    if organization.is_valid_token(token):
        username = organization.pending_invitations[token]
        user = User.objects.get(username = username)
        User.add_to_organization(username, organization_name)
        Organization.add_user(username, organization_name, token)
        current_app.logger.debug('User %s added to organization %s',username,organization_name)
        return jsonify(message = 'Member added'),HTTPStatus.OK
    current_app.logger.debug('User %s failed to join organization %s: invalid token',session['username'],organization_name)
    return jsonify(message = 'Invalid Token'),HTTPStatus.BAD_REQUEST

@organizations.route('/organization/<organization_name>/members', methods=['GET'])
@organization_no_banned_required
def return_members(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.members]
    return jsonify(members = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/members', methods=['DELETE'])
@user_no_banned_required
@organization_no_banned_required
def delete_member(organization_name):
    username = request.get_json(force = True)['username']
    organization = Organization.objects.get(organization_name = organization_name)
    user = User.objects.get(username = username)
    current_user = User.objects.get(username = session['username'])
    if not organization.is_owner(current_user) and not organization.is_moderator(current_user):
        current_app.logger.debug('Not owner nor moderator tried to delete a member from %s',organization_name)
        return jsonify(message = 'Only owner and moderators can delete a member'), HTTPStatus.FORBIDDEN
    if not organization.is_member(user):
        current_app.logger.debug('Tried to remove not a member from %s',organization_name)
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    organization.update(pull__members__ = user)
    current_app.logger.debug('Member %s deleted from organization %s',username,organization_name)
    return jsonify(message = 'Member deleted'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['GET'])
@user_no_banned_required
@organization_no_banned_required
def get_moderators(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.moderators]
    return jsonify(moderators = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def upgrade_to_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    current_user = User.objects.get(username = session['username'])
    if not organization.is_owner(current_user):
        return jsonify(message = 'You are not the owner'), HTTPStatus.FORBIDDEN
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_member(user):
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    organization.update(push__moderators = user)
    return jsonify(message = 'member is now a moderator'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['DELETE'])
@user_no_banned_required
@organization_no_banned_required
def delete_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    current_user = User.objects.get(username = session['username'])
    if not organization.is_owner(current_user):
        return jsonify(message = 'You are not the owner'), HTTPStatus.FORBIDDEN
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_moderator(user):
        return jsonify(message = 'User is not moderator'), HTTPStatus.BAD_REQUEST
    organization.update(pull__moderators = user)
    return jsonify(message = 'member is not a moderator anymore'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/locations', methods=['GET'])
@user_no_banned_required
@organization_no_banned_required
def get_organization_locations(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    locations = {}
    for member in organization.members:
        locations[member.username] = member.location
    return jsonify(locations=locations), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/word', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def add_forbidden_word(organization_name):
    current_user = User.objects.get(username = session['username'])
    organization = Organization.objects.get(organization_name = organization_name)
    if not organization.is_owner(current_user) and not organization.is_moderator(current_user):
        return jsonify(message = 'Only owner and moderators can add a forbidden word'), HTTPStatus.FORBIDDEN
    data = request.get_json(force = True)
    organization.update(add_to_set__forbidden_words=data['word'])
    return jsonify(message='Forbidden word added'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<word>', methods=['DELETE'])
@user_no_banned_required
@organization_no_banned_required
def delete_forbidden_word(organization_name, word):
    current_user = User.objects.get(username = session['username'])
    organization = Organization.objects.get(organization_name = organization_name)
    if not organization.is_owner(current_user) and not organization.is_moderator(current_user):
        return jsonify(message = 'Only owner and moderators can delete a forbidden word'), HTTPStatus.FORBIDDEN
    organization.update(pull__forbidden_words=word)
    return jsonify(message='Forbidden word deleted'), HTTPStatus.OK

#channels
@organizations.route('/organization/<organization_name>/channels', methods=['GET'])
@organization_no_banned_required
def get_channels(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    list_of_channels = [(channel.channel_name, channel.privacy()) for channel in organization.channels if session['username'] in channel.members]
    return jsonify(channels = list_of_channels), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/channels', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def create_channel(organization_name):
    data = request.get_json(force = True)
    channel_name = data['name']
    owner = session['username']
    privado = True if data['privado'] == "True" else False
    if Organization.create_channel(organization_name, channel_name, owner, privado):
        return '',HTTPStatus.CREATED
    return '',HTTPStatus.BAD_REQUEST

@organizations.route('/organization/<organization_name>/<channel_name>', methods=['GET'])
def get_channel(organization_name, channel_name):
    channel = Organization.get_channel(organization_name,channel_name)
    return jsonify(owner=channel.owner, is_private=channel.private,
                   description=channel.description, welcome_message=channel.welcome_message,
                   messages=len(channel.messages), members=len(channel.members)), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def change_channel_info(organization_name, channel_name):
    channel = Organization.get_channel(organization_name,channel_name)
    if not channel.is_owner(session['username']):
        return jsonify(message = 'You are not the owner'), HTTPStatus.FORBIDDEN
    data = request.get_json(force = True)
    private = data.get('privado', channel.private)
    description = data.get('description', channel.description)
    welcome_message = data.get('welcome_message', channel.welcome_message)
    channel.update(private = private)
    channel.update(description = description)
    channel.update(welcome_message = welcome_message)
    return jsonify(message = 'Information changed'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/members', methods=['GET'])
@user_no_banned_required
@organization_no_banned_required
def get_channel_members(organization_name, channel_name):
    members = Organization.get_channel_members(organization_name, channel_name)
    return jsonify(members = members), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/members', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def add_member_to_channel(organization_name, channel_name):
    data = request.get_json(force = True)
    member = data['name']
    if not Organization.has_member(organization_name, member):
        return jsonify(message = 'User is not member of this organization'), HTTPStatus.BAD_REQUEST
    added = Organization.add_member_to_channel(organization_name, channel_name, member)
    if not added:
        return jsonify(message = 'User is already in channel'), HTTPStatus.BAD_REQUEST
    FirebaseApi().send_invitation_notification_to_user(member, organization_name, channel_name)
    message = Message(message=Organization.get_welcome_message(organization_name), sender='tito',
                      timestamp=datetime.now(), creation_date=datetime.now(), type='text')
    FirebaseApi().send_message_to_users([member], message, organization_name, channel_name)
    return jsonify(message = 'User added'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/messages', methods=['GET'])
@user_no_banned_required
@organization_no_banned_required
def get_n_channel_messages(organization_name, channel_name):
    init = request.args.get('init', '')
    end = request.args.get('end', '')
    channel = Organization.get_channel(organization_name, channel_name)
    messages = channel.get_messages(int(init), int(end))
    list_of_msg = [(message.timestamp,message.sender,message.message,message.type) for message in messages]
    return jsonify(messages = list_of_msg), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/message', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def send_message(organization_name, channel_name):
    data = request.get_json(force = True)
    sender = data['sender']
    channel = Organization.get_channel(organization_name,channel_name)
    try:
        type = data['type']
        message = Message(message = data['message'], sender = sender, timestamp = datetime.now(), creation_date = datetime.now(), type = type)
    except KeyError:
        message = Message(message = data['message'], sender = sender, timestamp = datetime.now(), creation_date = datetime.now())
    if message.has_mention():
        (mentioned, command) = message.get_mentioned_and_command()
        if mentioned in channel.bots:
            FirebaseApi().send_message_to_users([session['username']], message, organization_name, channel_name)
            if mentioned == 'tito':
                response = requests.get(channel.bots['tito'] + command + '?user=' + session['username'] + '&org=' + organization_name + '&channel=' + channel_name)
            else:
                response = requests.post(channel.bots[mentioned], json={'query': command})
            message = Message(message=response.json()['message'], sender=mentioned, timestamp=datetime.now(), creation_date=datetime.now(), type='text')
            FirebaseApi().send_message_to_users([session['username']], message, organization_name, channel_name)
            return jsonify(message = 'Message sent'), HTTPStatus.OK
        if mentioned == 'all':
            FirebaseApi().send_notification_to_users(channel.members, organization_name, channel_name)
        elif mentioned not in channel.members:
            if not Organization.has_member(organization_name, mentioned):
                return jsonify(message = 'User is not member of this organization'), HTTPStatus.BAD_REQUEST
            Organization.add_member_to_channel(organization_name, channel_name, mentioned)
            FirebaseApi().send_invitation_notification_to_user(mentioned, organization_name, channel_name)
        else:
            FirebaseApi().send_notification_to_users([mentioned], organization_name, channel_name)
    message.replace_organization_forbidden_words(Organization.get_forbidden_words(organization_name))
    message.save()
    channel.update(push__messages = message)
    User.objects.get(username=sender).update(inc__sent_messages=1)
    response = FirebaseApi().send_message_to_users(channel.members, message, organization_name, channel_name)
    if not response:
        return jsonify(message = 'Firebase error'), HTTPStatus.SERVICE_UNAVAILABLE
    return jsonify(message = 'Message sent'),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/bot', methods=['POST'])
@user_no_banned_required
@organization_no_banned_required
def create_bot(organization_name, channel_name):
    data = request.get_json(force = True)
    bot_name = data['name']
    if User.objects(username=bot_name).count():
        return jsonify(message='Bot cannot have the same name than a user'), HTTPStatus.BAD_REQUEST
    bot_url = data['url']
    channel = Organization.get_channel(organization_name, channel_name)
    if bot_name in channel.bots:
        return jsonify(message='Duplicated bot name'), HTTPStatus.BAD_REQUEST
    channel.update(**{'set__bots__' + bot_name: bot_url})
    return jsonify(message='Bot created'), HTTPStatus.CREATED

@organizations.route('/organization/<organization_name>/<channel_name>/bot/<bot_name>', methods=['DELETE'])
@user_no_banned_required
@organization_no_banned_required
def delete_bot(organization_name, channel_name, bot_name):
    channel = Organization.get_channel(organization_name, channel_name)
    channel.update(**{'unset__bots__' + bot_name: 1})
    return jsonify(message='Bot deleted'), HTTPStatus.OK
