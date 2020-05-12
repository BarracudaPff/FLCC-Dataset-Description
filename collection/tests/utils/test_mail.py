from io import StringIO
from unittest import TestCase
from unittest.mock import patch

from parameterized import parameterized

from src.utils.mail import MailNotifier

_subject = "Lorem ipsum."
_info = "Some info without email"


class TestMail(TestCase):
    def test_mail_notifier_with_None_subject(self):
        email = "none"
        with self.assertRaises(TypeError):
            MailNotifier(None, email)

    def test_send_notification_pass_email(self):
        email = "none"
        notifier = MailNotifier(_subject, email)

        with patch('sys.stdout', new=StringIO()) as stdout_emulator:
            notifier.send_notification(_info)

            self.assertEqual(stdout_emulator.getvalue().strip(), _info)

    @parameterized.expand([
        ["wrong email", "no such email"],
        ["email is None", None]
    ])
    def test_send_notification_with_error(self, _, email):
        notifier = MailNotifier(_subject, email)
        with patch('sys.stdout', new=StringIO()) as stdout_emulator:
            notifier.send_notification(_info)
            output = stdout_emulator.getvalue().strip()

            self.assertTrue(output.startswith(_info))
            self.assertGreater(len(output), len(_info))

    @parameterized.expand([
        ["TypeError", TypeError],
        ["AssertionError", AssertionError],
    ])
    def test_send_error_pass_email(self, _, error):
        email = "none"
        notifier = MailNotifier(_subject, email)

        with patch('sys.stdout', new=StringIO()) as stdout_emulator:
            notifier.send_error(error)

            self.assertGreater(len(stdout_emulator.getvalue().strip()), 0)
