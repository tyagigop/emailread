

# create a table in the database sqlite3 if not present mail_stored

import sqlite3

def create_table():
    conn = sqlite3.connect('mails.db')
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS mail_stored
                (id INTEGER PRIMARY KEY,
                mail_id TEXT NOT NULL UNIQUE,
                subject TEXT,
                body TEXT)''')
    conn.commit()
    conn.close()

def check_if_mail_exists(mail_id):
    conn = sqlite3.connect('mails.db')
    c = conn.cursor()
    c.execute("SELECT * FROM mail_stored WHERE mail_id = ?", (mail_id,))
    result = c.fetchone()
    conn.close()
    if result:
        return True
    return False

def insert_mail(mail_id, subject, body):
    conn = sqlite3.connect('mails.db')
    c = conn.cursor()
    print("Inserting mail")
    c.execute("INSERT INTO mail_stored (mail_id, subject, body) VALUES (?, ?, ?)", (mail_id, subject, body))
    conn.commit()
    conn.close()