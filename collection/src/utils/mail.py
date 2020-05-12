import smtplib
import ssl

from src.config import password, sender_email

port = 465
smtp_server = "smtp.gmail.com"


class MailNotifier:

    def __init__(self, subject, receiver_email):
        if subject is None:
            raise TypeError("Subject can't be None!")
        self.subject = subject
        self.receiver_email = receiver_email
        self.password = password

    def send_notification(self, info):
        msg = str(info)
        self.__send_message(msg)

    def send_error(self, error: Exception):
        msg = f"Error happened!!!\n{str(error)}\n{str(error.__traceback__)}"
        self.__send_message(msg)

    def __send_message(self, msg: str):
        print(msg)
        if self.receiver_email != 'none':
            try:
                context = ssl.create_default_context()
                with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
                    server.login(sender_email, self.password)
                    server.sendmail(sender_email, self.receiver_email, self.subject + msg)
            except smtplib.SMTPAuthenticationError as e:
                print(f"{e.smtp_error.decode()}\nCheck password and sender_email in src/config.py")
