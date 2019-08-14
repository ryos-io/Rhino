package io.ryos.rhino.sdk.providers;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.exceptions.NoUserFoundException;
import io.ryos.rhino.sdk.users.oauth.OAuthUserImpl;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepository;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class OAuthUserProviderTest {

  private static final String TEST_USER_1 = "testUser1";
  private static final String TEST_USER_2 = "testUser2";
  private static final String TEST_PW = "foo";
  private static final String ACCESS_TOKEN = "ey1=";
  private static final String REFRESH_TOKEN = "ey2=";
  private static final String SCOPE = "openid";
  private static final String CLIENT_ID = "testClient";
  private static final String USER_ID = "1";
  private static final String US_REGION = "US";

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  CyclicUserSessionRepository<UserSession> repo;

  @Test
  public void testTake() {

    var oAuthUser = new OAuthUserImpl(null,
        TEST_USER_1,
        TEST_PW,
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        SCOPE,
        CLIENT_ID,
        USER_ID,
        US_REGION
    );

    var usUserSession = new UserSessionImpl(oAuthUser);

    when(repo.take()).thenReturn(usUserSession);

    var authUserProvider = new OAuthUserProvider(repo);
    assertThat(authUserProvider.take(), equalTo(usUserSession.getUser()));
  }

  @Test
  public void testTakeDifferentUsers() {

    var oAuthUser1 = new OAuthUserImpl(null,
        TEST_USER_1,
        TEST_PW,
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        SCOPE,
        CLIENT_ID,
        USER_ID,
        US_REGION
    );
    var usUserSession1 = new UserSessionImpl(oAuthUser1);
    var oAuthUser2 = new OAuthUserImpl(null,
        TEST_USER_2,
        TEST_PW,
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        SCOPE,
        CLIENT_ID,
        USER_ID,
        US_REGION
    );

    var usUserSession2 = new UserSessionImpl(oAuthUser2);
    when(repo.take()).thenReturn(usUserSession1, usUserSession2);
    var authUserProvider = new OAuthUserProvider(repo);
    assertThat(authUserProvider.take(usUserSession1.getUser()),
        not(equalTo(usUserSession1.getUser())));
  }

  @Test(expected = NoUserFoundException.class)
  public void testTakeSameUserWhileExpectingAnother() {

    var oAuthUser1 = new OAuthUserImpl(null,
        TEST_USER_1,
        TEST_PW,
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        SCOPE,
        CLIENT_ID,
        USER_ID,
        US_REGION
    );
    var userSession = new UserSessionImpl(oAuthUser1);
    when(repo.take()).thenReturn(userSession, userSession, userSession);
    var authUserProvider = new OAuthUserProvider(repo);
    assertThat(authUserProvider.take(userSession.getUser()),
        not(equalTo(userSession.getUser())));
  }
}
