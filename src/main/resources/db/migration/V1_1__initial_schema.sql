CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "USER" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    firstname VARCHAR(100),
    lastname VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    address TEXT,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'STUDENT', 'TEACHER'))
);

CREATE TABLE IF NOT EXISTS ADMIN (
    user_id UUID PRIMARY KEY,
    reference CHAR(8),
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES "USER"(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS TEACHER (
    user_id UUID PRIMARY KEY,
    reference CHAR(8),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES "USER"(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STUDENT (
    user_id UUID PRIMARY KEY,
    reference CHAR(8),
    status VARCHAR(50),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES "USER"(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COHORT (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    start_year INT,
    end_year INT
);

CREATE TABLE IF NOT EXISTS BRANCH (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code CHAR(2),
    name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS SEMESTER (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cohort_id UUID NOT NULL,
    semester_number INT,
    academic_year VARCHAR(50),
    CONSTRAINT fk_semester_cohort FOREIGN KEY (cohort_id) REFERENCES COHORT(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COURSE (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    semester_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    code VARCHAR(10),
    title VARCHAR(150),
    credits INT,
    CONSTRAINT fk_course_semester FOREIGN KEY (semester_id) REFERENCES SEMESTER(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_branch FOREIGN KEY (branch_id) REFERENCES BRANCH(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COURSE_TEACHER (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    course_id UUID NOT NULL,
    CONSTRAINT fk_courseteacher_teacher FOREIGN KEY (teacher_id) REFERENCES TEACHER(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_courseteacher_course FOREIGN KEY (course_id) REFERENCES COURSE(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS GROUPE (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name CHAR(2),
    cohort_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    CONSTRAINT fk_groupe_cohort FOREIGN KEY (cohort_id) REFERENCES COHORT(id) ON DELETE CASCADE,
    CONSTRAINT fk_groupe_branch FOREIGN KEY (branch_id) REFERENCES BRANCH(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COURSE_GROUP (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    course_id UUID NOT NULL,
    CONSTRAINT fk_coursegroup_group FOREIGN KEY (group_id) REFERENCES GROUPE(id) ON DELETE CASCADE,
    CONSTRAINT fk_coursegroup_course FOREIGN KEY (course_id) REFERENCES COURSE(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS EXAM (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL,
    title VARCHAR(150),
    weight NUMERIC(4, 2),
    exam_date DATE,
    CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES COURSE(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS GRADE (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    exam_id UUID NOT NULL,
    score NUMERIC(4, 2),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES STUDENT(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_exam FOREIGN KEY (exam_id) REFERENCES EXAM(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS GRADE_HISTORY (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_id UUID NOT NULL,
    user_id UUID NOT NULL,
    previous_score NUMERIC(4, 2),
    new_score NUMERIC(4, 2),
    reason TEXT,
    modification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gradehistory_grade FOREIGN KEY (grade_id) REFERENCES GRADE(id) ON DELETE CASCADE,
    CONSTRAINT fk_gradehistory_user FOREIGN KEY (user_id) REFERENCES "USER"(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STUDENT_GROUP_HISTORY (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    group_id UUID NOT NULL,
    start_date DATE,
    end_date DATE,
    change_reason TEXT,
    CONSTRAINT fk_sgh_student FOREIGN KEY (student_id) REFERENCES STUDENT(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_sgh_group FOREIGN KEY (group_id) REFERENCES GROUPE(id) ON DELETE CASCADE
);
