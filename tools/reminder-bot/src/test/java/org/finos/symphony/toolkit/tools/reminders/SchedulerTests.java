package org.finos.symphony.toolkit.tools.reminders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.finos.symphony.toolkit.stream.Participant;
import org.finos.symphony.toolkit.stream.cluster.LeaderService;
import org.finos.symphony.toolkit.tools.reminders.alerter.Scheduler;
import org.finos.symphony.toolkit.workflow.Workflow;
import org.finos.symphony.toolkit.workflow.content.Addressable;
import org.finos.symphony.toolkit.workflow.content.RoomDef;
import org.finos.symphony.toolkit.workflow.content.User;
import org.finos.symphony.toolkit.workflow.history.History;
import org.finos.symphony.toolkit.workflow.response.FormResponse;
import org.finos.symphony.toolkit.workflow.sources.symphony.handlers.ResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.api.model.StreamAttributes;
import com.symphony.api.model.StreamList;
import com.symphony.api.pod.StreamsApi;

@ExtendWith(MockitoExtension.class)
public class SchedulerTests {

    @Mock
    History history;

    @Mock
    ResponseHandler responseHandler;

    @Mock
    RoomDef roomDef;

    @Mock
    LeaderService leaderService;

    @Mock
    Participant self;

    @Mock
    StreamsApi streams;

    @Mock
    Workflow w;

    @InjectMocks
    Scheduler scheduler = new Scheduler();

    LocalDateTime expectedTime = LocalDateTime.now();


    @SuppressWarnings("unchecked")
	//@Test
    public void handleFeedLeaderTest(){
        when(history.getLastFromHistory(Mockito.any(Class.class),Mockito.any(Addressable.class))).thenReturn(reminderList());

        when(leaderService.isLeader(Mockito.any())).thenReturn(true);
        when(streams.v1StreamsListPost(null, null, 0, 50)).thenReturn(createStreams());

        scheduler.everyFiveMinutesWeekday();
        verify(responseHandler).accept(Mockito.any(FormResponse.class));
        ArgumentCaptor<FormResponse> argumentCaptor = ArgumentCaptor.forClass(FormResponse.class);
        verify(responseHandler).accept(argumentCaptor.capture());
        FormResponse fr = argumentCaptor.getValue();
        Reminder r = (Reminder)fr.getFormObject();
        Assertions.assertEquals(r.getLocalTime(),expectedTime);

        // reminder timefinder tests to chck formresponse

    }
    @SuppressWarnings("unchecked")
	//@Test
    public void handleFeedNonLeaderTest(){
        when(leaderService.isLeader(Mockito.any())).thenReturn(false);
        scheduler.everyFiveMinutesWeekday();
        verify(responseHandler, VerificationModeFactory.noInteractions()).accept(Mockito.any(FormResponse.class));

    }


      private StreamList createStreams(){
        StreamAttributes streamAttributes = new StreamAttributes();
        StreamList sl = new StreamList();
        streamAttributes.setId("1234");
        sl.add(streamAttributes);
        return sl;
      }

    private Optional<ReminderList> reminderList(){
        Reminder reminder = new Reminder();
        reminder.setDescription("Check at 9 pm");
        reminder.setLocalTime(expectedTime);
        reminder.setAuthor(getUser());
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(reminder);
        ReminderList rl = new ReminderList();
        rl.setTimeZone(ZoneId.of("Europe/London"));

        rl.setReminders(reminders);
        Optional<ReminderList> rrl = Optional.of(rl);
        return rrl;
    }

    private User getUser(){
        User user = new User() {
            @Override
            public String getAddress() {
                return "New Address";
            }

            @Override
            public String getId() {
                return "1234";
            }

            @Override
            public Type getTagType() {
                return null;
            }

            @Override
            public String getName() {
                return "Sherlock Holmes";
            }

            @Override
            public String getText() {
                return null;
            }
        };
        return user;

    }
}
