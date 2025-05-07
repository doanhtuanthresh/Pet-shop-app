import com.example.adopt_pet_app.data.model.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun createPost(
        userId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        imageUrl: String,
        location: String,
        description: String,
        createdAt: FieldValue = FieldValue.serverTimestamp()
    ): Task<Void> {
        val postId = firestore.collection("posts").document().id

        val post = Post(
            userId = userId,
            petName = petName,
            type = type,
            gender = gender,
            age = age,
            imageUrl = imageUrl,
            location = location,
            description = description,
            createdAt = null // sẽ được Firestore tự gán bằng Rule
        )

        return firestore.collection("posts")
            .document(postId)
            .set(post)
    }

    fun addToFavorites(userId: String, postId: String): Task<Void> {
        return firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(postId)
            .set(emptyMap<String, Any>())
    }

    fun getPosts(onResult: (List<Post>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    fun getUserPosts(userId: String, onResult: (List<Post>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    fun getAllPosts(
        onResult: (List<Post>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

}
