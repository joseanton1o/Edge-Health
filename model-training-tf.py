import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score
import os

# Load the dataset
dataset = pd.read_csv('SensorsCollect.csv', sep=';')

print(dataset.head())

# Drop 'sent' column
dataset = dataset.drop('sent', axis=1)

# Drop rows with "none" in userStatus
dataset = dataset[dataset.userStatus != 'none']

# Separate features and target
X_vector = dataset.drop(columns=['userStatus', 'timestamp'])
y_vector = dataset['userStatus']

# One-hot encode the target variable
y_vector = pd.get_dummies(y_vector)

# Split the data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X_vector, y_vector, test_size=0.2, random_state=42)

# Standardize the features
scaler = StandardScaler()
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

# Build a TensorFlow model
model = tf.keras.Sequential([
    tf.keras.layers.Dense(128, activation='relu', input_shape=(X_train.shape[1],)),
    tf.keras.layers.Dense(64, activation='relu'),
    tf.keras.layers.Dense(y_train.shape[1], activation='softmax')
])

# Compile the model
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

# Train the model
model.fit(X_train, y_train, epochs=20, batch_size=32, validation_split=0.2)

# Make predictions
y_pred = model.predict(X_test)
y_pred_classes = y_pred.argmax(axis=1)
y_test_classes = y_test.values.argmax(axis=1)

# Evaluate the model
accuracy = accuracy_score(y_test_classes, y_pred_classes)
print(f'Accuracy: {accuracy}')

# Create directory if it does not exist
save_dir = 'saved_model'
if not os.path.exists(save_dir):
    os.makedirs(save_dir)

# Save the model
model.save(os.path.join(save_dir, 'my_model.keras'))

# Load the model
loaded_model = tf.keras.models.load_model(os.path.join(save_dir, 'my_model.keras'))

# Make predictions using the loaded model
y_pred = loaded_model.predict(X_test)
y_pred_classes = y_pred.argmax(axis=1)
y_test_classes = y_test.values.argmax(axis=1)

# Evaluate the loaded model
accuracy = accuracy_score(y_test_classes, y_pred_classes)
print(f'Accuracy: {accuracy}')