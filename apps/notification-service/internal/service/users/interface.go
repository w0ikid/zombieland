package users

import (
	"context"
)

type Service interface {
	GetEmailByID(ctx context.Context, id string) (string, error)
}
