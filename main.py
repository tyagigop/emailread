import imaplib
import email
from email.header import decode_header
import time
import schedule
import os
from datetime import datetime, timedelta
from database import create_table, check_if_mail_exists, insert_mail


# Email account credentials
username = 'pythonanywhere00001@gmail.com'
password = ''
imap_server = 'imap.gmail.com'  


# Connect to the server and login to the account
def connect_to_email():
    mail = imaplib.IMAP4_SSL(imap_server)
    mail.login(username, password)
    return mail

def get_text_from_message(message):
    if message.is_multipart():
        print("multipart")
        return get_text_from_multipart(message)
    else:
        print("Plain Text")
        return message.get_payload()

def get_text_from_multipart(multipart):
    result = []
    for part in multipart.walk():
        if part.get_content_type() == 'text/plain':
            result.append(part.get_payload(decode=True).decode())
        elif part.get_content_type() == 'text/html':
            result.append(part.get_payload(decode=True).decode())
    return ''.join(result)

def fetch_messages(mail_ids, mail):
    for i in mail_ids:
        mail_id = str(i, 'utf-8')
        if check_if_mail_exists(mail_id):
            print("Mail already exists.")
        else:
            status, data = mail.fetch(i, '(RFC822)')
            if status == 'OK':
                message = email.message_from_bytes(data[0][1])
                from_email = email.utils.parseaddr(message['From'])[1]
                # print(from_email)
                if from_email == 'hemantty0208@gmail.com':
                    print("Subject:", message['Subject'])
                    body = get_text_from_message(message)
                    insert_mail(mail_id, message['Subject'], body)
                    print("Body:", body)


def fetch_latest_emails():
    mail = connect_to_email()
    mail.select("inbox")
    print("Checking for new emails...")
    date_format = "%d-%b-%Y"
    today = datetime.today()

    # Define a dynamic since_date (e.g., 7 days ago)
    days_ago = 1
    since_date = today - timedelta(days=days_ago)
    today = today + timedelta(days=1)

    # Format the dates for the IMAP search query
    since_date_formatted = since_date.strftime(date_format)
    before_date_formatted = today.strftime(date_format)
    print(since_date_formatted, before_date_formatted)

    typ, msg_ids = mail.search(None, '(SINCE "{}" BEFORE "{}")'.format(since_date_formatted, before_date_formatted))
    email_ids = msg_ids[0].split()
    fetch_messages(email_ids, mail)
    mail.logout()

create_table()
schedule.every(1).seconds.do(fetch_latest_emails)


while True:
    print("Running job...")
    # fetch_latest_emails()
    print(schedule.get_jobs())
    schedule.run_pending()
    time.sleep(1)
