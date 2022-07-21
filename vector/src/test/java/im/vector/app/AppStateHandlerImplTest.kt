/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app

import im.vector.app.test.fakes.FakeActiveSessionDataSource
import im.vector.app.test.fakes.FakeActiveSessionHolder
import im.vector.app.test.fakes.FakeAnalyticsTracker
import im.vector.app.test.fakes.FakeSession
import im.vector.app.test.fakes.FakeUiStateRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.matrix.android.sdk.api.session.room.model.RoomSummary

internal class AppStateHandlerImplTest {

    private val spaceId = "spaceId"
    private val spaceSummary: RoomSummary = mockk()
    private val session = FakeSession.withRoomSummary(spaceSummary)

    private val sessionDataSource = FakeActiveSessionDataSource()
    private val uiStateRepository = FakeUiStateRepository()
    private val activeSessionHolder = FakeActiveSessionHolder(session)
    private val analyticsTracker = FakeAnalyticsTracker()

    private val appStateHandler = AppStateHandlerImpl(
            sessionDataSource.instance,
            uiStateRepository,
            activeSessionHolder.instance,
            analyticsTracker,
    )

    @Before
    fun setup() {
        justRun { uiStateRepository.storeSelectedSpace(any(), any()) }
        every { spaceSummary.roomId } returns spaceId
    }

    @Test
    fun `given selected space doesn't exist, when getCurrentSpace, then return null`() {
        val currentSpace = appStateHandler.getCurrentSpace()

        currentSpace shouldBe null
    }

    @Test
    fun `given selected space exists, when getCurrentSpace, then return selected space`() {
        appStateHandler.setCurrentSpace(spaceId, session)

        val currentSpace = appStateHandler.getCurrentSpace()

        currentSpace shouldBe spaceSummary
    }

    @Test
    fun `given persistNow is true, when setCurrentSpace, then immediately persist to ui state`() {
        appStateHandler.setCurrentSpace(spaceId, session, persistNow = true)

        uiStateRepository.verifyStoreSelectedSpace(spaceId, session)
    }

    @Test
    fun `given persistNow is false, when setCurrentSpace, then don't immediately persist to ui state`() {
        appStateHandler.setCurrentSpace(spaceId, session, persistNow = false)

        uiStateRepository.verifyStoreSelectedSpace(spaceId, session, inverse = true)
    }

    @Test
    fun `given is forward navigation and no current space, when setCurrentSpace, then null added to backstack`() {
        appStateHandler.setCurrentSpace(spaceId, session, isForwardNavigation = true)

        val backstack = appStateHandler.getSpaceBackstack()

        backstack.size shouldBe 1
        backstack.first() shouldBe null
    }

    @Test
    fun `given is forward navigation and is in space, when setCurrentSpace, then previous space added to backstack`() {
        appStateHandler.setCurrentSpace(spaceId, session, isForwardNavigation = true)
        appStateHandler.setCurrentSpace("secondSpaceId", session, isForwardNavigation = true)

        val backstack = appStateHandler.getSpaceBackstack()

        backstack.size shouldBe 2
        backstack shouldBeEqualTo listOf(null, spaceId)
    }

    @Test
    fun `given is not forward navigation, when setCurrentSpace, then previous space not added to backstack`() {
        appStateHandler.setCurrentSpace(spaceId, session, isForwardNavigation = false)

        val backstack = appStateHandler.getSpaceBackstack()

        backstack.size shouldBe 0
    }

    @Test
    fun `when setCurrentSpace, then space is emitted to selectedSpaceFlow`() = runTest {
        appStateHandler.setCurrentSpace(spaceId, session)

        val currentSpace = appStateHandler.getSelectedSpaceFlow().first().orNull()

        currentSpace shouldBeEqualTo spaceSummary
    }

    @Test
    fun `given current space exists, when getSafeActiveSpaceId, then return current space id`() {
        appStateHandler.setCurrentSpace(spaceId, session)

        val activeSpaceId = appStateHandler.getSafeActiveSpaceId()

        activeSpaceId shouldBeEqualTo spaceId
    }

    @Test
    fun `given current space doesn't exist, when getSafeActiveSpaceId, then return current null`() {
        val activeSpaceId = appStateHandler.getSafeActiveSpaceId()

        activeSpaceId shouldBe null
    }
}
