insert into esl_member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT ) values (1, 'tester', 'tester', 'tester', '2000-01-01' ,'tester@esl.com', current_date, 1);
insert into esl_member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT ) values (2, 'tester2', 'tester 2', 'tester 2', '2000-01-01' ,'tester2@esl.com', current_date, 0);
insert into esl_member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT ) values (3, 'auth0-56489432', 'Chi on', 'Tam', '2000-01-01' ,'tam.chi.on@gmail.com', current_date, 0);
insert into esl_member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT ) values (4, 'auth0-56489433', '', '', '2000-01-01' ,'hoi.nam@esl.com', current_date, 0);

insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (1, 0, 'Testing 1', -1, -1, 'testing dictation', 0, true, CURRENT_DATE, 'Any', 1, CURRENT_DATE, false, false, 2.5, 1, 1, null, 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (2, 0, 'Dictation will be deleted', -1, -1, 'dictation will be deleted by test case', 0, true, CURRENT_DATE, 'Any', 1, CURRENT_DATE, false, false, 2.5, 1, 1, '', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (3, 0, 'Sentence Dictation 1', -1, -1, 'testing dictation', 0, true, CURRENT_DATE, 'Any', 1, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (4, 0, 'Tam dictation 1', -1, -1, 'first dictation', 0, true, CURRENT_DATE, 'SeniorSecondary', 3, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (5, 0, 'Tam school dictation', -1, -1, 'P1 term a exam 1', 0, true, CURRENT_DATE, 'JuniorPrimary', 3, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (6, 0, 'Tam school dictation', -1, -1, 'P1 term a exam 2', 0, true, CURRENT_DATE, 'JuniorPrimary', 3, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (7, 0, 'Tam school dictation', -1, -1, 'P2 test A', 0, true, CURRENT_DATE, 'JuniorPrimary', 3, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC, ARTICLE, SENTENCE_LEN, WORD_CONTAIN_SPACE, SOURCE) values (8, 0, 'Tam school dictation', -1, -1, 'P2 test B', 0, true, CURRENT_DATE, 'JuniorPrimary', 3, CURRENT_DATE, false, false, 2.5, 1, 1, 'It is a sentence dictation', 'Normal', false, 'FillIn');

insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE) values (1, 'apple', 2, 1, 1, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE) values (2, 'zoo', 2, 1, 1, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE) values (3, 'apple', 0, 0, 2, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE) values (4, 'zoo', 0, 0, 2, '2018-01-01');

insert into MEMBER_SCORE VALUES (1, CURRENT_DATE, CURRENT_DATE, 1, 2147483647,RANDOM(100));
insert into MEMBER_SCORE VALUES (2, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(CURRENT_DATE,'yyyyMM'),RANDOM(100));
insert into MEMBER_SCORE VALUES (3, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(DATEADD('MONTH', -1, CURRENT_DATE),'yyyyMM'),RANDOM(100));
insert into MEMBER_SCORE VALUES (4, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(DATEADD('MONTH', -2, CURRENT_DATE),'yyyyMM'),RANDOM(100));
insert into MEMBER_SCORE VALUES (5, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(DATEADD('MONTH', -3, CURRENT_DATE),'yyyyMM'),RANDOM(100));
insert into MEMBER_SCORE VALUES (6, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(DATEADD('MONTH', -4, CURRENT_DATE),'yyyyMM'),RANDOM(100));
insert into MEMBER_SCORE VALUES (7, CURRENT_DATE, CURRENT_DATE, 1, FORMATDATETIME(DATEADD('MONTH', -5, CURRENT_DATE),'yyyyMM'),RANDOM(100));

insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (3, 'aeroplane', 'ˋɛərəplein', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukaer/ukaerog005.mp3', '2008-02-07 10:46:19', 'AIR_007.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/aeroplane.mp3', -1, 500);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (20, 'day', 'dei', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukd/ukdat/ukdatag015.mp3', '2008-02-07 10:46:20', null, 'https://s.yimg.com/tn/dict/dreye/live/m/-day.mp3', 1187.38, 88);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (27, 'eat', 'i:t', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uke/ukeas/ukeasil014.mp3', '2008-02-07 10:46:20', 'CRTN135.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/eat.mp3', 208.64, 488);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (4, 'air', 'ɛə', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukaha/ukaha__020.mp3', '2008-02-07 10:46:19', null, 'https://s.yimg.com/tn/dict/dreye/live/f/air.mp3', 179.05, 577);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (7, 'boy', 'bɔi', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukb/ukbou/ukbount028.mp3', '2008-02-07 10:46:19', 'IMG_109.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/boy.mp3', 191.65, 544);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (11, 'baby', 'ˋbeibi', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukazt/ukazt__013.mp3', '2008-02-07 10:46:20', null, 'https://s.yimg.com/tn/dict/dreye/live/f/baby.mp3', 107.88, 946);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (10, 'bread', 'bred', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukb/ukbra/ukbraw_014.mp3', '2008-02-07 10:46:20', 'BRED_058.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/bread.mp3', 29.97, 2401);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (12, 'cat', 'kæt', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukc/ukcas/ukcaste011.mp3', '2008-02-07 10:46:20', 'CRTN0380.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/cat.mp3', 46.27, 1784);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (15, 'cake', 'keik', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukc/ukcaj/ukcajun002.mp3', '2008-02-07 10:46:20', 'DESRT025.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/cake.mp3', 31.53, 2312);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (16, 'cloud', 'klaud', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukc/ukclo/ukclosu008.mp3', '2008-02-07 10:46:20', 'OTHR_054.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/cloud.mp3', 33.47, 2199);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (21, 'desk', 'desk', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukd/ukdes/ukdesir006.mp3', '2008-02-07 10:46:20', null, 'https://s.yimg.com/tn/dict/dreye/live/f/desk.mp3', 50.74, 1564);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (1, 'apple', 'ˋæpl', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukapp/ukappen014.mp3', '2008-02-07 10:46:19', 'FRUT_032.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/apple.mp3', 18.9, 3193);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (5, 'bat', 'bæt', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukb/ukbas/ukbashf030.mp3', '2008-02-07 10:46:19', 'CRTN0058.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/bat.mp3', 17.38, 3264);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (17, 'cap', 'kæp', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukc/ukcan/ukcant_015.mp3', '2008-02-07 10:46:20', 'HATS_015.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/cap.mp3', 27.47, 2574);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (18, 'duck', 'dʌk', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukd/ukdub/ukdubio005.mp3', '2008-02-07 10:46:20', 'BIRD_250.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/duck.mp3', 18.12, 3274);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (43, 'juice', 'dʒu:s', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukj/ukjud/ukjudic014.mp3', '2008-02-07 10:46:20', 'DRNA_137.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/juice.mp3', 18.25, 3258);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (6, 'bee', 'bi:', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukazt/ukazt__003.mp3', '2008-02-07 10:46:19', 'CRTN0106.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/bee.mp3', 12.08, 4191);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (9, 'butterfly', 'ˋbʌtəflai', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukb/ukbut/ukbutte003.mp3', '2008-02-07 10:46:19', 'CRTN0267.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/butterfly.mp3', 8.69, 4996);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (31, 'fox', 'fɔks', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukf/ukfou/ukfours005.mp3', '2008-02-07 10:46:20', 'CRTN0997.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/fox.mp3', 9.18, 4810);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (41, 'ink', 'iŋk', 'http://dictionary.cambridge.org/media/english/uk_pron/u/uki/ukina/ukinaug010.mp3', '2008-02-07 10:46:20', 'SUPP_200.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/ink.mp3', 9.8, 4641);
insert into phonetic_question (PHONETICQUESTION_ID, WORD, IPA, PRONOUNCED_LINK, CREATED_DATE, PIC_FILE_NAME, PRONOUNCED_LINK_BACKUP, FREQUENCY, RANK) VALUES (50, 'lion', 'ˋlaiən', 'http://dictionary.cambridge.org/media/english/uk_pron/u/ukl/uklin/uklink_010.mp3', '2008-02-07 10:46:20', 'CRTN1177.jpg', 'https://s.yimg.com/tn/dict/dreye/live/f/lion.mp3', 12.85, 4018);

insert into member_vocabulary (MEMBER_ID, WORD, CORRECT, WRONG) VALUES (1, 'test-apple', 3, 0);
insert into member_vocabulary (MEMBER_ID, WORD, CORRECT, WRONG) VALUES (4, 'test-apple', 0, 0);
