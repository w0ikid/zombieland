package consumers

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/segmentio/kafka-go"
	"github.com/w0ikid/zombieland/apps/notification-service/internal/service/users"
	"github.com/w0ikid/zombieland/pkg/models"
	"go.uber.org/zap"
)

type DistrictCriticalHandler struct {
	sendNotificationUsecase interface {
		Execute(ctx context.Context, notification models.Notification) (*models.Notification, error)
	}
	userService users.Service
	logger      *zap.SugaredLogger
}

func NewDistrictCriticalHandler(
	sendNotificationUsecase interface {
		Execute(ctx context.Context, notification models.Notification) (*models.Notification, error)
	},
	userService users.Service,
	logger *zap.SugaredLogger,
) *DistrictCriticalHandler {
	return &DistrictCriticalHandler{
		sendNotificationUsecase: sendNotificationUsecase,
		userService:             userService,
		logger:                  logger.Named("district_critical_handler"),
	}
}

func (h *DistrictCriticalHandler) Handle(ctx context.Context, msg kafka.Message) error {
	var event models.DistrictCriticalEvent
	if err := json.Unmarshal(msg.Value, &event); err != nil {
		h.logger.Errorw("failed to unmarshal district.critical", "error", err)
		return err
	}

	h.logger.Infow("district.critical received",
		"district_id", event.DistrictID,
		"district_name", event.DistrictName,
		"survival_index", event.SurvivalIndex,
		"owner_id", event.OwnerID,
	)

	// 1. Get owner email
	email, err := h.userService.GetEmailByID(ctx, event.OwnerID)
	if err != nil {
		h.logger.Errorw("failed to get owner email", "owner_id", event.OwnerID, "error", err)
		return err
	}

	// 2. Map to Notification Model
	subject := fmt.Sprintf("CRITICAL STATUS: District %s is falling!", event.DistrictName)
	body := fmt.Sprintf(`
		<div style="font-family: 'Courier New', monospace; max-width: 600px; margin: 20px auto; padding: 20px; border: 2px solid #ff4d4d; border-radius: 5px; background-color: #1a1a1a; color: #f0f0f0;">
			<h2 style="color: #ff4d4d; border-bottom: 1px solid #ff4d4d; padding-bottom: 10px; text-transform: uppercase;">[SYSTEM ALERT] Critical Status Detected</h2>
			<p>Attention, Commander,</p>
			<p>Our sensors indicate that <strong>%s</strong> (ID: %d) has reached a critical survival index.</p>
			
			<div style="background-color: #2a2a2a; padding: 15px; border-left: 5px solid #ff4d4d; margin: 20px 0;">
				<p style="margin: 5px 0;"><strong>CURRENT SURVIVAL INDEX:</strong> <span style="color: #ff4d4d; font-size: 24px;">%d%%</span></p>
				<p style="margin: 5px 0;"><strong>STATUS:</strong> CRITICAL</p>
			</div>

			<p style="color: #cccccc;">%s</p>

			<p style="margin-top: 30px; font-weight: bold; color: #ffbc00;">ACTION REQUIRED IMMEDIATELY. Dispatch reinforcements or evacuate essential personnel.</p>
			
			<hr style="border: 0; border-top: 1px solid #333; margin: 20px 0;">
			<p style="color: #666; font-size: 11px; text-align: center;">ZOMBIELAND COMMAND & CONTROL SYSTEM | AUTOMATED NOTIFICATION</p>
		</div>
	`, event.DistrictName, event.DistrictID, event.SurvivalIndex, event.Message)

	notification, err := h.sendNotificationUsecase.Execute(ctx, models.Notification{
		UserID:   event.OwnerID,
		Type:     models.TypeDistrictCritical,
		Channel:  models.ChannelEmail,
		Subject:  subject,
		Body:     body,
		Metadata: map[string]any{"email": email, "district_id": event.DistrictID},
	})
	if err != nil {
		h.logger.Errorw("failed to send district critical notification", "district_id", event.DistrictID, "user_id", event.OwnerID, "error", err)
		return err
	}

	h.logger.Infow("district critical notification sent", "notification_id", notification.ID, "user_id", event.OwnerID, "status", notification.Status)
	return nil
}
