import torch

# x=2, y=3, α = 4, β=5
x = torch.tensor(2.0, requires_grad=False)
y = torch.tensor(3.0, requires_grad=False)
α = torch.tensor(4.0, requires_grad=True)
β = torch.tensor(5.0, requires_grad=True)

# loss = (αx+β-y)^2
loss = pow((α * x + β - y), 2)

loss.backward()

print(loss)
print(α.grad)
print(β.grad)
print(x.grad)
