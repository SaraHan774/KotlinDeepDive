package clean.domain

// 현실 세계의 개념을 표현하는 것이 도메인 모델
data class Student(
    val name: String,
    val age: Int,
)

data class Course(
    val title: String,
    val maxStudents: Int,
)

data class Enrollment(
    val student: Student,
    val course: Course,
    val grade: Double?
)


// 도메인 로직을 추가하기
// 학생이 수업에 등록할 때 최대 정원을 초과하면 안 된다.

class EnrollmentService {
    private val enrollments = mutableListOf<Enrollment>()

    fun enroll(student: Student, course: Course): String {
        val enrolledStudents = enrollments.count { it.course == course }
        return if (enrolledStudents < course.maxStudents) {
            enrollments.add(Enrollment(student, course, null))
            "${student.name} has enrolled ${course.title}"
        } else {
            "${course.title} has exceeded max students"
        }
    }
}

// 도메인 계층에서 데이터를 관리했지만, 현실세계에서는 데이터를 DB에서 가져와야 한다
// 따라서 데이터 계층과 도메인 계층을 문리하는 연습을 해보자

// 데이터 저장을 담당하는 repository 만들기

interface IStudentRepository {
    fun save(student: Student)
    fun findAll(): List<Student>
}

class StudentRepository : IStudentRepository {
    private val students = mutableListOf<Student>()

    override fun save(student: Student) {
        students.add(student)
    }

    override fun findAll(): List<Student> {
        return students
    }
}

// 이제 도메인 계층과, 데이터 계층이 분리되었다
// 도메인 계층은 데이터 저장방식 (DB, file, network) 에 의존하지 않고, 오직 비즈니스 로직에만 집중할 수 있다.
