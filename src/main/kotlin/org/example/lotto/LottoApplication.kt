package org.example.lotto

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import kotlin.random.Random

@SpringBootApplication
@EnableSwagger2
class LottoApplication

fun main(args: Array<String>) {
	runApplication<LottoApplication>(*args)
}

@RestController
class UserController {
	@PostMapping("/user/login")
	fun loginUser(@RequestBody request: LoginRequest): ResponseEntity<String> {
		if (request.email == "example@example.com" && request.password == "password123") {
			return ResponseEntity.ok("로그인 성공!")
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.")
		}
	}

	@PostMapping("/user/create")
	fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<String> {
		val fullName = request.fullName
		val email = request.email
		val password = request.password

		return ResponseEntity.ok("새로운 사용자가 생성: $fullName, $email, $password")
	}
}

data class LoginRequest(
	val email: String,
	val password: String
)

data class CreateUserRequest(
	val fullName: String,
	val email: String,
	val password: String
)

@RestController
class LottoController {
	@GetMapping("/lotto")
	fun generateLottoNumbers(): ResponseEntity<LottoResponse> {
		val numbers = List(5) {
			List(7) { Random.nextInt(1, 46) }
		}
		val lottoResponse = LottoResponse(numbers)
		return ResponseEntity.ok(lottoResponse)
	}

	@PostMapping("/lotto")
	fun processLottoRequest(@RequestBody request: LottoRequest): ResponseEntity<LottoResponse> {
		val numbers = request.numbers
		val lottoResponse = LottoResponse(numbers)
		return ResponseEntity.ok(lottoResponse)
	}
}

data class LottoRequest(
	val numbers: List<List<Int>>
)

data class LottoResponse(
	val numbers: List<List<Int>>
)

@RestController
class LottoCheckController {
	@PostMapping("/lotto/check")
	fun checkLottoResults(@RequestBody request: List<LottoCheckRequest>): ResponseEntity<List<LottoCheckResponse>> {
		val results = mutableListOf<LottoCheckResponse>()
		request.forEach { lottoCheckRequest ->
			val index = lottoCheckRequest.index
			val winningNumbers = lottoCheckRequest.winningNumbers
			val ticketResults = mutableListOf<TicketResult>()

			lottoCheckRequest.results.forEach { ticket ->
				val ticketNumbers = ticket.numbers
				val correctNumbers = ticket.correctNumbers
				val result = checkResult(ticketNumbers, winningNumbers.numbers, winningNumbers.bonusNumber)
				ticketResults.add(TicketResult(ticketNumbers, correctNumbers, result))
			}

			val lottoCheckResponse = LottoCheckResponse(index, winningNumbers, ticketResults)
			results.add(lottoCheckResponse)
		}

		return ResponseEntity.ok(results)
	}

	fun checkResult(ticketNumbers: List<Int>, winningNumbers: List<Int>, bonusNumber: Int): String {
		val correctCount = ticketNumbers.intersect(winningNumbers).count()
		val hasBonusNumber = ticketNumbers.contains(bonusNumber)

		return when (correctCount) {
			6 -> "1등입니다!"
			5 -> if (hasBonusNumber) "2등입니다!" else "3등입니다!"
			4 -> "4등입니다!"
			3 -> "5등입니다!"
			else -> "낙첨입니다.."
		}
	}

}

data class LottoCheckRequest(
	val index: Int,
	val winningNumbers: LottoNumbers,
	val results: List<Ticket>
)

data class LottoNumbers(
	val numbers: List<Int>,
	val bonusNumber: Int
)

data class Ticket(
	val numbers: List<Int>,
	val correctNumbers: LottoNumbers
)

data class LottoCheckResponse(
	val index: Int,
	val winningNumbers: LottoNumbers,
	val results: List<TicketResult>
)

data class TicketResult(
	val numbers: List<Int>,
	val correctNumbers: LottoNumbers,
	val result: String
)

@Bean
fun api(): Docket {
	return Docket(DocumentationType.SWAGGER_2)
		.select()
		.apis(RequestHandlerSelectors.basePackage("org.example.lotto"))
		.paths(PathSelectors.any())
		.build()
}
