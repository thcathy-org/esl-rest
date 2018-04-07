insert into member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT )
values (1, 'tester', 'tester', 'tester', '2000-01-01' ,'tester@esl.com', current_date, 1);
insert into member (MEMBER_ID, USER_ID, LAST_NAME, FIRST_NAME, birthday, EMAIL_ADDRESS, CREATED_DATE, TOTAL_WORD_LEARNT )
values (2, 'tester2', 'tester 2', 'tester 2', '2000-01-01' ,'tester2@esl.com', current_date, 0);

insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC )
values (1, 0, 'Testing 1', -1, -1, 'testing dictation', 0, true, CURRENT_DATE, 'Any', 1, CURRENT_DATE, false, false, 2.5, 1, 1);
insert into dictation (ID, TOTAL_RECOMMENDED, TITLE, SUITABLE_MIN_AGE, SUITABLE_MAX_AGE, DESCRIPTION, TOTAL_ATTEMPT, SHOW_IMAGE, LAST_MODIFY_DATE, SUITABLE_STUDENT, MEMBER_ID, CREATED_DATE, NOT_ALLOW_IPA, NOT_ALLOW_RAND_CHAR, RATING, TOTAL_RATED, IS_PUBLIC )
values (2, 0, 'Dictation will be deleted', -1, -1, 'dictation will be deleted by test case', 0, true, CURRENT_DATE, 'Any', 1, CURRENT_DATE, false, false, 2.5, 1, 1);

insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE)
values (1, 'apple', 2, 1, 1, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE)
values (2, 'zoo', 2, 1, 1, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE)
values (3, 'apple', 0, 0, 2, '2018-01-01');
insert into dictation_vocab (ID, WORD, TOTAL_CORRECT, TOTAL_WRONG, DICTATION_ID, CREATED_DATE)
values (4, 'zoo', 0, 0, 2, '2018-01-01');