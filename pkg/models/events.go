package models

import (
	"github.com/google/uuid"
)

type BalanceUpdatedPayload struct {
	AccountID     uuid.UUID  `json:"account_id"`
	Amount        int64      `json:"amount"`
	OperationType string     `json:"operation_type"` // DEPOSIT, WITHDRAW, HOLD, REFUND
	ReferenceID   *uuid.UUID `json:"reference_id,omitempty"`
	NewBalance    int64      `json:"new_balance"`
}

type DistrictCriticalEvent struct {
	DistrictID    int    `json:"districtId"`
	DistrictName  string `json:"districtName"`
	SurvivalIndex int    `json:"survivalIndex"`
	OwnerID       string `json:"ownerId"`
	Message       string `json:"message"`
}
