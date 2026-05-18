SELECT F.flightCode, F.departureAt, F.isDelayed, 
	   M.loginId, M.password, M.name, M.birthDate, M.phoneNumber, M.passportNumber,
	   M.passportExpiryDate, M.isAdult, M.grade
FROM FlightBook B JOIN Member M ON B.memberId = M.memberId
				  JOIN Flight F ON B.flightId = F.flightId
WHERE M.passportNumber = ? AND B.reservationCode = ?