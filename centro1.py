import cancha as c
import reservas as r
class Centro:
    def __init__(self, nombre, direccion):
        self.nombre = nombre
        self.direccion = direccion
        self.lista_canchas = []
        self.lista_clientes = []

centro = Centro("Centro 1", "Calle 1")

cancha1 = c.Cancha()
cancha1.crear_cancha()

centro.lista_canchas.append(cancha1)

deportes_disponibles = []

cancha1.mostrar_deportes_disponibles(centro, deportes_disponibles)
cancha1.mostrar_canchas_para_deporte("Futbol", centro)