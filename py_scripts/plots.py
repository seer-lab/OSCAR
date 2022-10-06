import matplotlib.pyplot as plt
import numpy as np

percentages = range(5)
percentage_names = ["10%", "25%", "50%", "75%", "100%"]
similarity = [1-0.8754, 1-0.8666, 1-0.8657, 1-0.8689, 1-0.872]
unique_inter = [225.1/250, 239.6/250, 247.222222222222/250, 249.89/250, 249.67/250]

# plot our data along a line
fig, ax = plt.subplots()
fig.suptitle('Probabilistic Noise Triggering Results')
ax.set_title('(Average values for high concurrency in account, lottery and pingpong programs)')
ax.plot(percentages, similarity, '-', color='tab:blue')
ax.set_ylabel('Avg. Difference') #Similarity')
ax.set_xticks(percentages)
ax.set_xticklabels(percentage_names)
ax.set_xlabel('Noise Triggering Probability')
plt.ylim((0,1))

ax2 = ax.twinx()
ax2.plot(percentages, unique_inter, '-', color='tab:red')
ax2.set_ylabel('Avg. Unique interleavings')
ax2.set_xticks(percentages)
ax.set_xticklabels(percentage_names)
ax2.set_ylim(0,1)

# create a confidence band of +/- 10% error
y_lower = np.add([0.00863, 0.00530, 0.00457, 0.00440, 0.00437], similarity)
y_upper = np.add([-0.00863, -0.00530, -0.00457, -0.00440, -0.00437], similarity)

# plot our confidence band
ax.fill_between(percentages, y_lower, y_upper, alpha=0.2, color='tab:blue')

plt.show()
