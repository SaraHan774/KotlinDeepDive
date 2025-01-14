package clean

data class Employee(
    val name: String,
    val age: Int,
    val pay: Int,
    val workingHours: Int
) {
//    fun calculatePay(calculator: PayCalculator) {
//        calculator.calculatePay(this)
//    }
//
//    fun reportHours(reporter: HourReporter) {
//        reporter.reportHours(this)
//    }
//
//    fun save(saver: EmployeeSaver) {
//        saver.saveEmployee(this)
//    }
}

class EmployeeFacade {
    private val payCalculator = PayCalculator()
    private val hourReporter = HourReporter()
    private val employeeSaver = EmployeeSaver()

    fun calculatePay(employee: Employee) {
        payCalculator.calculatePay(employee)
    }

    fun reportHours(employee: Employee) {
        hourReporter.reportHours(employee)
    }

    fun saveEmployee(employee: Employee) {
        employeeSaver.saveEmployee(employee)
    }
}

class PayCalculator {
    fun calculatePay(employee: Employee) {}
}

class HourReporter {
    fun reportHours(employee: Employee) {

    }
}

class EmployeeSaver {
    fun saveEmployee(employee: Employee) {

    }
}