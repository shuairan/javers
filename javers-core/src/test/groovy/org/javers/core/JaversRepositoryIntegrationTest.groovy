package org.javers.core

import org.javers.core.diff.changetype.ValueChange
import org.javers.core.model.DummyUser
import org.javers.core.model.SnapshotEntity
import org.javers.core.snapshot.SnapshotsAssert
import org.javers.repository.api.InMemoryRepository
import spock.lang.Specification

import static org.javers.core.JaversBuilder.javers
import static org.javers.core.metamodel.object.InstanceId.InstanceIdDTO.instanceId
import static org.javers.test.builder.DummyUserBuilder.dummyUser

class JaversRepositoryIntegrationTest extends Specification {

    Javers javers

    def setup() {
        def repo = new InMemoryRepository()
        javers = javers().registerJaversRepository(repo).build()
    }

    def "should store state history in JaversRepository"() {
        given:
        def ref = new SnapshotEntity(id:2)
        def cdo = new SnapshotEntity(id: 1, entityRef: ref)
        javers.commit("author",cdo) //v. 1
        ref.intProperty = 5
        javers.commit("author",cdo) //v. 2

        when:
        def snapshots = javers.getStateHistory(2, SnapshotEntity, 10)

        then:
        def cdoId = instanceId(2,SnapshotEntity)
        SnapshotsAssert.assertThat(snapshots)
                .hasSize(2)
                .hasSnapshot(cdoId, "1.0", [id:2])
                .hasSnapshot(cdoId, "2.0", [id:2, intProperty:5])

        snapshots[0].commitMetadata.commitId == "2.0"
        snapshots[1].commitMetadata.commitId == "1.0"
    }

    def "should compare property values with latest from repository"() {
        given:
        def user = dummyUser("John").withAge(18).build()
        javers.commit("login", user)

        when:
        user.age = 19
        javers.commit("login", user)
        def history = javers.getChangeHistory("John", DummyUser, 100)

        then:
        history.size() == 1
        history[0] instanceof ValueChange
        history[0].affectedCdoId == instanceId("John", DummyUser)
        history[0].property.name == "age"
        history[0].left == 18
        history[0].right == 19
    }

    def "should create empty commit when nothing changed"() {
        given:
        def cdo = new SnapshotEntity(listOfEntities:    [new SnapshotEntity(id:2), new SnapshotEntity(id:3)])
        def firstCommit = javers.commit("author",cdo)

        when:
        def secondCommit = javers.commit("author",cdo)

        then:
        firstCommit.snapshots.size() == 3
        !secondCommit.snapshots
        !secondCommit.diff.changes
    }
}