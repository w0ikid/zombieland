package service

import (
	"github.com/w0ikid/zombieland/apps/notification-service/internal/repo"
	"github.com/w0ikid/zombieland/apps/notification-service/internal/service/notification"
	"github.com/w0ikid/zombieland/apps/notification-service/internal/service/users"
	"github.com/w0ikid/zombieland/pkg/smtpclient"
	"github.com/w0ikid/zombieland/pkg/zitadel"
	"go.uber.org/zap"
)

type Service struct {
	NotificationService notification.Service
	UsersService        users.Service
}

func New(repositories *repo.Repository, zitadelClient *zitadel.Client, smtpClient *smtpclient.Client, logger *zap.SugaredLogger) *Service {
	logger = logger.Named("service")
	return &Service{
		NotificationService: notification.NewService(repositories.Notification, smtpClient, logger),
		UsersService:        users.NewService(zitadelClient, logger),
	}
}
