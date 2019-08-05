import smtplib
import ssl

# TODO: add password and email here for email logging
port = 465
password = ""
smtp_server = "smtp.gmail.com"
sender_email = ""


class MailNotifier:

    def __init__(self, subject, receiver_email='mrm.kikll@gmail.com'):
        self.subject = subject
        self.receiver_email = receiver_email
        self.password = password

    def send_notification(self, info):
        msg = str(info)
        self.__send_message(msg)

    def send_error(self, error):
        msg = "Error happened!!!\n" + str(error)
        self.__send_message(msg)

    def __send_message(self, msg: str):
        print(msg)
        if self.receiver_email != 'none':
            try:
                context = ssl.create_default_context()
                with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
                    server.login(sender_email, self.password)
                    server.sendmail(sender_email, self.receiver_email, self.subject + msg)
            except Exception as e:
                print(e)
